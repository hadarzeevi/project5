// IHstorage.h
#ifndef IHSTORAGE_H
#define IHSTORAGE_H
#include "storage.h"
#include "bloom_filter.h"
#include "blacklist.h"
#include <memory>

using namespace std;
class IHstorage {
public:
    virtual ~IHstorage() = default; // destructot
    
    virtual void saveBloomFilter(const BloomFilter& bloomFilter) = 0;
    virtual shared_ptr<BloomFilter> loadBloomFilter(size_t size, vector<shared_ptr<IHFunction>> hashFunctions) = 0;
    
    virtual void saveBlacklist(const unordered_set<string>& blacklist) = 0;
    virtual unordered_set<string> loadBlacklist() = 0;
};

#endif // IHSTORAGE_H