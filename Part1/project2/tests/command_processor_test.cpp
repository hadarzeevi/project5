#include "gtest/gtest.h"
#include "../src/command_processor.h"
#include "../src/blacklist.h"
#include "../src/bloom_filter.h"
#include "../src/HashUtils.h"
#include "../src/IHstorage.h"
#include <memory>

using namespace std;

class MockStorage : public Storage {
public:
    MockStorage(const string& bloomFilterFilePath = "mock_bloom.txt", const string& blacklistFilePath = "mock_blacklist.txt")
        : Storage(bloomFilterFilePath, blacklistFilePath) {}

    void saveBloomFilter(const BloomFilter& bloomFilter) override {}
    unique_ptr<BloomFilter> loadBloomFilter(size_t size, vector<shared_ptr<IHFunction>> hashFunctions) override {
        return nullptr;
    }
    void saveBlacklist(const unordered_set<string>& blacklist) override {}
    unordered_set<string> loadBlacklist() override {
        return {};
    }
};

class CommandProcessorTest : public ::testing::Test {
protected:
    void SetUp() override {
        vector<shared_ptr<IHFunction>> hashFunctions;
        hashFunctions.push_back(make_shared<HashFunction>());
        auto bloomFilter = make_unique<BloomFilter>(100, move(hashFunctions));
        auto blacklist = make_unique<Blacklist>(move(bloomFilter));
        auto storage = make_unique<MockStorage>();

        processor = make_unique<CommandProcessor>(move(blacklist), move(storage));
    }

    unique_ptr<CommandProcessor> processor;
};

TEST_F(CommandProcessorTest, ProcessesValidAddCommand) {
    string command = "POST www.example.com";
    string result = processor->processCommand(command);
    EXPECT_FALSE(result.empty()); 
}

TEST_F(CommandProcessorTest, ProcessesValidCheckCommand) {
    processor->processCommand("POST www.example.com");

    string command = "GET www.example.com";
    string result = processor->processCommand(command);
    EXPECT_FALSE(result.empty());

    command = "DELETE www.example.com";
    result = processor->processCommand(command);
    EXPECT_FALSE(result.empty());
}

TEST_F(CommandProcessorTest, RejectsInvalidCommand) {
    string command = "ADD www.example.com"; 
    string result = processor->processCommand(command);
    EXPECT_EQ(result, "400 Bad Request");
}

TEST_F(CommandProcessorTest, RejectsInvalidCommandFormat) {
    string command = "bad format";
    string result = processor->processCommand(command);
    EXPECT_EQ(result, "400 Bad Request");
}


int main(int argc, char** argv) {
    ::testing::InitGoogleTest(&argc, argv);
    int result = RUN_ALL_TESTS();

    if (result == 0) {
        cout << "All tests for command processor passed!\n";
    }

    return result;
}
