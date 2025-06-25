#ifndef BLOOMFILTER_H
#define BLOOMFILTER_H

#include "IHFunction.h"
#include <vector>
#include <string>
#include <functional>
#include <unordered_set>
#include <memory>

using namespace std;

class BloomFilter {
    public:
        // constructor gets size of array & hash vector 
        BloomFilter(size_t size, const vector<shared_ptr<IHFunction>>& hashObjects);
        void add(const string& url);
        bool mightContain(const string& url) const;
        const vector<bool>& getBitArray() const;
        void setBitArray(const vector<bool>& bitArray);
        
    private:
        size_t size_;
        vector<bool> bitArray_;
        vector<shared_ptr<IHFunction>> objectHashers_;
        
};
// this func is outside bc in the future we will want to use it without an object
vector<shared_ptr<IHFunction>> createHashFunctions(const vector<int>& hashSpecs);

#endif // BLOOMFILTER_H