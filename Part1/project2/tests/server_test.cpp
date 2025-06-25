
#include "gtest/gtest.h"
#include "server.h"
#include <thread>
#include <string>
#include <cstring>
#include <unistd.h>
#include <arpa/inet.h>

using namespace std;

class ServerTest : public ::testing::Test {
protected:
    int test_port = 12345;

    int create_server_socket() {
        return create_listening_socket(test_port);
    }

    void start_dummy_server_responsive() {
        std::thread([&]() {
            int server_sock = create_listening_socket(test_port);
            int client_sock = accept_client(server_sock);
    
            char buffer[1024];
            ssize_t bytes_received = recv(client_sock, buffer, sizeof(buffer), 0);
    
            std::string client_message(buffer, bytes_received);
            if (client_message == "invalid config") {
                
            } else if (client_message == "sample command") {
                const char* msg = "Expected command response";
                send(client_sock, msg, strlen(msg), 0);
            } else {
                const char* msg = "Dummy server OK";
                send(client_sock, msg, strlen(msg), 0);
            }
    
            cleanup(client_sock);
            cleanup(server_sock);
        }).detach();
        std::this_thread::sleep_for(std::chrono::milliseconds(100));
    }
    void simulate_client_send_config(const std::string& config) {
        int sock = socket(AF_INET, SOCK_STREAM, 0);
        if (sock < 0) {
            perror("Error creating client socket");
            return;
        }
    
        sockaddr_in sin {};
        sin.sin_family = AF_INET;
        sin.sin_port = htons(test_port);
        sin.sin_addr.s_addr = INADDR_ANY;
    
        if (connect(sock, (struct sockaddr*)&sin, sizeof(sin)) < 0) {
            perror("Error connecting to server");
            close(sock);
            return;
        }
    
        cout << "Client sending config: " << config << endl;
        send(sock, config.c_str(), config.size(), 0);
    
        char buffer[1024];
        ssize_t bytes_received = recv(sock, buffer, sizeof(buffer), 0);
        if (bytes_received < 0) {
            perror("Error receiving data from server");
        } else {
            cout << "Received response: " << string(buffer, bytes_received) << endl;
        }
    
        close(sock);
    }
    

    void simulate_client_send_command(const std::string& command) {
        int sock = socket(AF_INET, SOCK_STREAM, 0);
        ASSERT_GE(sock, 0);
        
        sockaddr_in sin {};
        sin.sin_family = AF_INET;
        sin.sin_port = htons(test_port);
        sin.sin_addr.s_addr = INADDR_ANY;

        ASSERT_EQ(connect(sock, (struct sockaddr*)&sin, sizeof(sin)), 0);
        send(sock, command.c_str(), command.size(), 0);
        
        char buffer[1024];
        ssize_t bytes_received = recv(sock, buffer, sizeof(buffer), 0);
        ASSERT_GT(bytes_received, 0);
        
        std::string response(buffer, bytes_received);
        // Verify that the response is as expected (depends on what your processor returns)
        EXPECT_TRUE(response.find("Expected command response") != std::string::npos);
        
        close(sock);
    }

    void simulate_client_send_invalid_config(const std::string& config) {
        int sock = socket(AF_INET, SOCK_STREAM, 0);
        ASSERT_GE(sock, 0);
        
        sockaddr_in sin {};
        sin.sin_family = AF_INET;
        sin.sin_port = htons(test_port);
        sin.sin_addr.s_addr = INADDR_ANY;

        this_thread::sleep_for(chrono::milliseconds(100));  // עיכוב קטן
        ASSERT_EQ(connect(sock, (struct sockaddr*)&sin, sizeof(sin)), 0);
        send(sock, config.c_str(), config.size(), 0);
        
        char buffer[1024];
        ssize_t bytes_received = recv(sock, buffer, sizeof(buffer), 0);
    
      
        EXPECT_EQ(bytes_received, 0);  
        
        close(sock);
    }
    
};

// Test 1: Test creating listening socket
TEST_F(ServerTest, CreateListeningSocket) {
    int sock = create_server_socket();
    EXPECT_GE(sock, 0);
    cleanup(sock);  // Ensure cleanup happens after the test
}

// Test 2: Test accepting a client connection
TEST_F(ServerTest, AcceptClient) {
    int server_sock = create_server_socket();
    ASSERT_GE(server_sock, 0);

    Simulate a client connection
    int client_sock = accept_client(server_sock);
    EXPECT_GE(client_sock, 0);
    cleanup(client_sock);  // Ensure client socket is cleaned up after the test
    cleanup(server_sock);  // Ensure server socket is cleaned up after the test
}

// Test 3: Test handling client configuration
TEST_F(ServerTest, HandleClientConfig) {
    std::string valid_config = "100 1 2 3";  // Example valid config
    simulate_client_send_config(valid_config);
}

// Test 4: Test handling invalid configuration
TEST_F(ServerTest, HandleInvalidConfig) {
    test_port = 12346;
    start_dummy_server_responsive();
    std::string invalid_config = "invalid config";  // Example of invalid config
    simulate_client_send_invalid_config(invalid_config);
}

// Test 5: Test handling client command
TEST_F(ServerTest, HandleClientCommand) {
    test_port = 12347;
    start_dummy_server_responsive();
    std::string command = "sample command";  // Example command
    simulate_client_send_command(command);
}

TEST_F(ServerTest, CleanupSocket) {
    int sock = socket(AF_INET, SOCK_STREAM, 0);
    ASSERT_GE(sock, 0);
    cleanup(sock);
}

int main(int argc, char** argv) {
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS(); 
}
