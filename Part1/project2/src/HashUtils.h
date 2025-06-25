#ifndef HASHUTILS_H
#define HASHUTILS_H

#include "IHFunction.h"
#include <memory>
#include <string>
#include <functional>

using namespace std;

size_t basicHash(const string& str);  // basic hash function

size_t wrappedHash(const string& str, size_t numWraps);  // load the hash function multipul time

// class for creating hash, get string return hash - override to IHfunction
class HashFunction : public IHFunction {
public:
    size_t operator()(const string& input) const override;  
    virtual ~HashFunction() = default;
};

// class for wrap the hash function multipul time
class HashWrapper : public IHFunction {
private:
    shared_ptr<IHFunction> baseHashFunc;  // basic func
    size_t numWraps;  // num we got from the input

public:
    // contructor
    HashWrapper(shared_ptr<IHFunction> baseFunc, size_t wraps)
        : baseHashFunc(baseFunc), numWraps(wraps) {}

    size_t operator()(const string& input) const override; 
    virtual ~HashWrapper() = default;
};

#endif // HASHUTILS_H

