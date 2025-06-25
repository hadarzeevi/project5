#ifndef IHFUNCTION_H
#define IHFUNCTION_H

#include <string>

using namespace std;

class IHFunction {
public:

    virtual ~IHFunction() = default; // destructor
    virtual size_t operator()(const string& input) const = 0; 
};

#endif // IHFUNCTION_H
