#include "blacklist.h"
#include "storage.h"
#include "IHstorage.h"
#include "bloom_filter.h"
#include <iostream>

using namespace std;
//constructor
Blacklist::Blacklist(shared_ptr<BloomFilter> bloomFilter)
    : bloomFilter(move(bloomFilter)) {
}
// add url
void Blacklist::addUrl(const string& url) {
    bloomFilter->add(url);
    exactBlacklist.insert(url);
}

// check if a url is in the bloom filter using might contain (bloom filter)
bool Blacklist::isBlacklisted(const string& url) const {
    return bloomFilter->mightContain(url);
}

// check if a url is in the real black list
bool Blacklist::isBlacklistedExact(const string& url) const {
    return exactBlacklist.find(url) != exactBlacklist.end();
}

// get bloomFilter
shared_ptr<BloomFilter>& Blacklist::getBloomFilter() {
    return bloomFilter;
}

// get Exact Blacklist 
const unordered_set<string>& Blacklist::getExactBlacklist() const {
    return exactBlacklist;
}

//set Blacklist
void Blacklist::setExactBlacklist(const unordered_set<string>& blacklist) {
    exactBlacklist = blacklist;
}

void Blacklist::removeUrl(const string& url) {
    exactBlacklist.erase(url);
}