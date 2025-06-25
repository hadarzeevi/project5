#ifndef BLACKLIST_H
#define BLACKLIST_H

// #include "storage.h"     
// #include "IHstorage.h"   
#include "bloom_filter.h"
#include <string>
#include <unordered_set>
#include <memory>

using namespace std;

class Blacklist {
private:
    shared_ptr<BloomFilter> bloomFilter;
    unordered_set<string> exactBlacklist; // the real blacklist! contains the url

public:
    // constructor
    Blacklist(shared_ptr<BloomFilter> bloomFilter);
    void addUrl(const string& url);
    bool isBlacklisted(const string& url) const; // check if a url is in the bloom filter using might contain (bloom filter)
    bool isBlacklistedExact(const string& url) const; // check if a url is in the black list
    void removeUrl(const string& url);
    shared_ptr<BloomFilter>& getBloomFilter(); // get
    const unordered_set<string>& getExactBlacklist() const; // get
    void setExactBlacklist(const unordered_set<string>& blacklist); // set
};

#endif // BLACKLIST_H
