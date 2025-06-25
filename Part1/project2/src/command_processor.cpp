#include "command_processor.h"
#include <sstream>
#include <regex>
using namespace std;

CommandProcessor::CommandProcessor(shared_ptr<Blacklist> blacklist, shared_ptr<Storage> storage)
    : blacklist(move(blacklist)), storage(move(storage)) {
}
CommandProcessor::~CommandProcessor() = default;
string CommandProcessor::processCommand(const string& commandLine) {
    istringstream iss(commandLine);
    int commandType;
    string command;
    string url;
    
    if (!(iss >> command >> url)) {
        return "false";  // Parse error
    }
    if (command == "POST") {
        commandType = 1;
    } else if (command == "GET") {
        commandType = 2;
    } else if (command == "DELETE"){
        commandType = 3;
    } else if (command == "CHECK") { 
    commandType = 5;
    } else {
        commandType = 4;
    }
    // check if the url is valid
    static const regex urlPattern(R"(^((https?:\/\/)?(www\.)?([a-zA-Z0-9-]+\.)+[a-zA-Z0-9]{2,})(\/\S*)?$)");
    if (!regex_match(url, urlPattern)) {
        return "400 Bad Request"; // we dont want to print anything, we want to move on without using it
    }
    switch (commandType) {
        case 1:  // Add URL to blacklist
            blacklist->addUrl(url);
            storage->saveBloomFilter(*blacklist->getBloomFilter());
            storage->saveBlacklist(blacklist->getExactBlacklist());
            return "201 Created";
            
        case 2:  // Check if URL is blacklisted
            {
                bool bloomResult = blacklist->isBlacklisted(url);
                string s1 = bloomResult ? "true" : "false";
                if (bloomResult) {
                    bool exactResult = blacklist->isBlacklistedExact(url);
                    string s2 = exactResult ? "true" : "false";
                    return "200 Ok \n\n" + s1 +" " + s2;
                }
                
                return "200 Ok \n\n" + s1;
                
            }
        
        case 3:
            if (blacklist->isBlacklistedExact(url)) {
                blacklist->removeUrl(url);
                return "204 No Content";
            } 
            return "404 Not Found";

        case 5: { // CHECK
        bool isBlack = blacklist->isBlacklisted(url);
        return isBlack ? "true" : "false";
}
            
        default:
            return "400 Bad Request";  // Unknown command
    }
}

