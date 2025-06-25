#include "HashUtils.h"
#include <functional>
using namespace std;
// Returns hash(str) - basic hash, calculate the hash value of a string (std::hash)
size_t basicHash(const string& str) {
    return hash<string>{}(str);
}

// wrappedHash
size_t wrappedHash(const string& str, size_t numWraps) {
    hash<string> hasher;
    size_t hashValue = hasher(str); 
    for (size_t i = 0; i < numWraps; ++i) {
        hashValue = hasher(to_string(hashValue));  
    }
    return hashValue;
}

// // Returns hash(str) - basic hash, calculate the hash value of a string (std::hash)
size_t HashFunction::operator()(const string& input) const {
    return basicHash(input);  
}

// class for wrap the hash function multipul time
size_t HashWrapper::operator()(const string& input) const {
    size_t hashValue = (*baseHashFunc)(input);  
    for (size_t i = 0; i < numWraps; ++i) {
        hashValue = hash<size_t>{}(hashValue);  // if num = 2 -> h(h(x))
    }
    return hashValue;
}
