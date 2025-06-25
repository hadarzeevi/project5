#include <memory>
#include "command_processor.h"

int create_listening_socket(int port);

int accept_client(int server_sock);

void handle_client(int client_sock, shared_ptr<CommandProcessor> processor);

void cleanup(int sock);

int start(int server_port, shared_ptr<CommandProcessor> processor);