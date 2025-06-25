import socket
import sys

def main():
    # Check if correct number of arguments are provided
    if len(sys.argv) != 3:
        print(f"Usage: {sys.argv[0]} <server_ip> <server_port>")
        sys.exit(1)
    
    # Get server IP and port from command line arguments
    server_ip = sys.argv[1]
    server_port = int(sys.argv[2])
    
    # Create socket tcp
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    
    try:
        # Connect to the server
        s.connect((server_ip, server_port))
        
        # Get Bloom filter configuration from user
        while True:
            config = input("")
            if not config:
                continue
            
            # Send configuration to server
            s.sendall(config.encode('utf-8'))
            
            
            # Get configuration response
            data = s.recv(4096)
            if data == "error":
                continue
            break

        # Now enter command loop
        while True:
            msg = input("")
            if not msg:
                continue  # Skip empty input

            try:
                s.sendall(msg.encode('utf-8'))
                data = s.recv(4096)

                if not data:
                    print("Connection closed by server.")
                    break

                response = data.decode('utf-8').strip()
                if response.lower() == "error":
                    # התגובה לא טובה — נחזור לבקש קלט מהמשתמש
                    continue

                # אם קיבלנו תגובה טובה — נדפיס ונסיים (או נמשיך ללולאה הבאה)
                print(response)

            except Exception:
                continue  # אם הייתה שגיאה — נבקש קלט מחדש

    except ConnectionRefusedError:
        print(f"Connection refused. Make sure the server is running on {server_ip}:{server_port}.")
    except KeyboardInterrupt:
        print("\nClient terminated by user.")
    except Exception as e:
        print(f"An error occurred: {e}")
    finally:
        # Always close the socket
        s.close()
        print("Connection closed")

if __name__ == "__main__":
    main()