#include <iostream>
#include <vector>
#include <string>
#include "bloom_filter.h"
#include "blacklist.h"
#include "HashUtils.h"
#include "storage.h"
#include "server.h"   
#include <sstream>
#include <functional>
#include "command_processor.h"
#include <memory>
#include <regex>
using namespace std;

int main(int argc, char* argv[]) {
    if (argc < 3) {
        cout << "Usage: " << argv[0] << " <port> <arraySize> <hashSeed1> <hashSeed2> ..." << endl;
        return 1;
    }

    const int server_port = atoi(argv[1]);
    const size_t arraySize = static_cast<size_t>(atoi(argv[2]));

    vector<int> hashSpecs;
    for (int i = 3; i < argc; ++i) {
        hashSpecs.push_back(atoi(argv[i]));
    }

    if (hashSpecs.empty()) {
        cerr << "At least one hash function seed is required." << endl;
        return 1;
    }

    (void)system("mkdir -p ./data");

    // צור את הפילטר המשותף וכל הרכיבים פעם אחת
    auto hashObjs = createHashFunctions(hashSpecs);
    auto bloomFilterPtr = make_shared<BloomFilter>(arraySize, hashObjs);
    auto storagePtr = make_shared<Storage>("./data/bloom.dat", "./data/blacklist.txt");
    auto blacklistPtr = make_shared<Blacklist>(bloomFilterPtr);
    auto processor = make_shared<CommandProcessor>(blacklistPtr, storagePtr);

    return start(server_port, processor);
}
