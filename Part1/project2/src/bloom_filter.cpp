#include "bloom_filter.h"
#include "HashUtils.h"
#include <fstream>
#include <iostream>

using namespace std;

// BloomFilter constructor 
BloomFilter::BloomFilter(size_t size, const vector<shared_ptr<IHFunction>>& hashObjects)
    : size_(size), objectHashers_(hashObjects), bitArray_(size, false) {}

// function for adding the url to the blacklist, and turn on bits
void BloomFilter::add(const string& url) {
    // loop on hash func, calculate the hash value 
    for (const auto& hash : objectHashers_) {
        size_t hashValue = (*hash)(url);  
        bitArray_[hashValue % size_] = true; // turn on bits
    }
}


// function for checking the url if the bits turned on
bool BloomFilter::mightContain(const string& url) const {
    for (const auto& hash : objectHashers_) {
        size_t hashValue = (*hash)(url);
        if (!bitArray_[hashValue % size_]) return false;
    }
    return true;
}

// Function to create hash functions based on input
vector<shared_ptr<IHFunction>> createHashFunctions(const vector<int>& hashSpecs) {
    vector<shared_ptr<IHFunction>> hashFunctions;
    for (int spec : hashSpecs) {
        // Creating shared_ptr of IHFunction
        hashFunctions.push_back(make_shared<HashWrapper>(make_shared<HashFunction>(), spec));
    }
    return hashFunctions;
}
// getBitArray
const vector<bool>& BloomFilter::getBitArray() const {
    return bitArray_;  // מחזיר הפניה ל-vector של ה-bit array
}

// setBitArray
void BloomFilter::setBitArray(const vector<bool>& newBitArray) {
    bitArray_ = newBitArray;  // מעדכן את ה-bit array במערך החדש
}
