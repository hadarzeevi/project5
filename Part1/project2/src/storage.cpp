// storage.cpp
#include "storage.h"
#include <fstream>
#include <iostream>
#include "bloom_filter.h"
#include "blacklist.h"

using namespace std;

// constructor, gets 2 pathes - one for bloom filter, second for black list
Storage::Storage(const string& bloomFilterFilePath, const string& blacklistFilePath)
    : bloomFilterFilePath(bloomFilterFilePath), blacklistFilePath(blacklistFilePath) {
}

// save the bloom filter to a binary file - include size & bitarray
void Storage::saveBloomFilter(const BloomFilter& bloomFilter) {
    ofstream file(bloomFilterFilePath, ios::binary);
    if (!file) {
        return;
    }
    
    auto bitArray = bloomFilter.getBitArray();
    size_t size = bitArray.size();
    
    // Write size
    file.write(reinterpret_cast<const char*>(&size), sizeof(size));
    
    // Write bit array (compressed to bytes)
    vector<char> bytes((size + 7) / 8, 0);
    for (size_t i = 0; i < size; ++i) {
        if (bitArray[i]) {
            bytes[i / 8] |= (1 << (i % 8));
        }
    }
    
    file.write(bytes.data(), bytes.size());
}

shared_ptr<BloomFilter> Storage::loadBloomFilter(size_t size, vector<shared_ptr<IHFunction>> hashFunctions) {
// load bloom filter from a file, if doesn't exsist, create new one
    ifstream file(bloomFilterFilePath, ios::binary);
    if (!file) {
        // File doesn't exist or can't be opened - return empty bloom filter
        return make_unique<BloomFilter>(size, move(hashFunctions));
    }
    
    // Read size
    size_t storedSize;
    file.read(reinterpret_cast<char*>(&storedSize), sizeof(storedSize));
    
    if (storedSize != size) {
        // wring size - return empty bloom filter with requested size
        return make_unique<BloomFilter>(size, move(hashFunctions));
    }
    
    // Read bit array (compressed as bytes)
    vector<char> bytes((size + 7) / 8, 0);
    file.read(bytes.data(), bytes.size());
    
    vector<bool> bitArray(size, false);
    for (size_t i = 0; i < size; ++i) {
        if (bytes[i / 8] & (1 << (i % 8))) {
            bitArray[i] = true;
        }
    }
    
    auto bloomFilter = make_unique<BloomFilter>(size, move(hashFunctions));
    bloomFilter->setBitArray(bitArray);
    return bloomFilter;
}

// save the blacklist to a text file 
void Storage::saveBlacklist(const unordered_set<string>& blacklist) {
    ofstream file(blacklistFilePath);
    if (!file) {
        cerr << "Failed to open file for writing: " << blacklistFilePath << endl;
        return;
    }
    
    for (const auto& url : blacklist) {
        file << url << "\n";
    }
}
// load the black list from a text file and returen it as unordered_set
unordered_set<string> Storage::loadBlacklist() {
unordered_set<string> blacklist;
    ifstream file(blacklistFilePath);
    if (!file) {
        return blacklist;  // Empty set if file doesn't exist
    }
    
    string url;
    while (getline(file, url)) {
        if (!url.empty()) {
            blacklist.insert(url);
        }
    }
    
    return blacklist;
}