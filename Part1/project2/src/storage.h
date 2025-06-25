// storage.h
#ifndef STORAGE_H
#define STORAGE_H

#include "IHstorage.h"
#include <string>

using namespace std;

class Storage : public IHstorage {
private:
    string bloomFilterFilePath;
    string blacklistFilePath;

public:
    Storage(const string& bloomFilterFilePath, const string& blacklistFilePath);
    
    // save the bloom filter to a binary file
    void saveBloomFilter(const BloomFilter& bloomFilter) override;
    // load bloom filter from a file
    shared_ptr<BloomFilter> loadBloomFilter(size_t size, vector<shared_ptr<IHFunction>> hashFunctions) override;

    //save the blacklist to a text file 
    void saveBlacklist(const unordered_set<string>& blacklist) override;
    // load the black list from a text file
    unordered_set<string> loadBlacklist() override;
};

#endif // STORAGE_H
