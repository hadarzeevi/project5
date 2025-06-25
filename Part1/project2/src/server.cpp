#include <iostream>
#include <sys/socket.h>
#include <stdio.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <string.h>
#include <vector>
#include <sstream>
#include <memory>
#include <regex>
#include "bloom_filter.h"
#include "blacklist.h"
#include "HashUtils.h"
#include "storage.h"
#include "command_processor.h"
#include <thread>


using namespace std;
class SocketException : public exception {
    public:
        explicit SocketException(const string& msg) : message(msg) {}
        const char* what() const noexcept override {
            return message.c_str();
        }
    private:
        string message;
    };

// create socket
int create_listening_socket(int port) { 
    int sock = socket(AF_INET, SOCK_STREAM, 0); // Create TCP socket
    if (sock < 0) { // check if works 
        throw SocketException("Error creating socket");        
        return -1;
    }
    
    struct sockaddr_in sin; // Use sin to avoid conflicting with built-in sin function
    memset(&sin, 0, sizeof(sin));   // Initialize sockaddr_in structure
    sin.sin_family = AF_INET;
    sin.sin_addr.s_addr = INADDR_ANY;   // Bind to all available network interfaces
    sin.sin_port = htons(port); // Set the port number

    
    // Bind socket to the specified port
    if (bind(sock, (struct sockaddr*)&sin, sizeof(sin)) < 0) {
        throw SocketException("Error binding socket");
        close(sock);
        return -1;
    }

    // Listen for incoming connections
    if (listen(sock, 10) < 0) {
        throw SocketException("Error listening on socket");
        close(sock);
        return -1;
    }

    return sock;
}

// Accept a connection from a client
int accept_client(int server_sock) {
    struct sockaddr_in client_sin;
    unsigned int addr_len = sizeof(client_sin);
    int client_sock = accept(server_sock, (struct sockaddr*)&client_sin, &addr_len);
    if (client_sock < 0) {
        throw SocketException("Error accepting client");
    } 
    return client_sock;
}

// Receive a line from client
string receive_line(int client_sock) {
    char buffer[4096];
    memset(buffer, 0, sizeof(buffer));  // Clear the buffer
    
    int read_bytes = recv(client_sock, buffer, sizeof(buffer) - 1, 0);
    if (read_bytes <= 0) {
        return "";
    }
    
    return string(buffer, read_bytes);
}

// Send response to client
bool send_response(int client_sock, const string& response) {
    int sent_bytes = send(client_sock, response.c_str(), response.size(), 0);
    if (sent_bytes < 0) {
        throw SocketException("Error sending to client");
        return false;
    }
    return true;
}
// close socket
void cleanup(int sock) {
    close(sock);
}
void handle_client(int client_sock, shared_ptr<CommandProcessor> processor) {
    send_response(client_sock, "Ready for commands.");

    while (true) {
        string command = receive_line(client_sock);
        if (command.empty()) break;

        string response = processor->processCommand(command);

        if (response.find("\n\n") == string::npos)
            response += "\n\n";

        if (!send_response(client_sock, response)) break;
    }

    cleanup(client_sock);
}

int start(int server_port, shared_ptr<CommandProcessor> processor) {
    int server_sock = create_listening_socket(server_port);
    if (server_sock < 0) return 1;

    while (true) {
        int client_sock = accept_client(server_sock);
        if (client_sock >= 0) {
            thread client_thread([client_sock, processor]() {
                try {
                    handle_client(client_sock, processor);
                } catch (const exception& e) {
                    cerr << "Exception in client thread: " << e.what() << endl;
                }
                cleanup(client_sock);
            });
            client_thread.detach();
        }
    }

    return 0;
}
