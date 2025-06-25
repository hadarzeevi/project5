#ifndef COMMAND_PROCESSOR_H
#define COMMAND_PROCESSOR_H

#include "blacklist.h"
#include "storage.h"
#include <string>
#include <memory>
#include <iostream>
using namespace std;

class CommandProcessor {
private:
    shared_ptr<Blacklist> blacklist;
    shared_ptr<Storage> storage;

public:
    ~CommandProcessor(); // destructor
    CommandProcessor(shared_ptr<Blacklist> blacklist, shared_ptr<Storage> storage); // constructor
    
    string processCommand(const string& commandLine);

};

#endif