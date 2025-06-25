# שם הקובץ הסופי
TARGET = my_program

# כל קבצי המקור
SRCS = main.cpp bloom_filter.cpp blacklist.cpp HashUtils.cpp storage.cpp command_processor.cpp server.cpp

# הפקת קבצי אובייקט מתוך המקור
OBJS = $(SRCS:.cpp=.o)

# הקומפיילר והפלגים
CXX = g++  
CXXFLAGS = -std=c++17 -Wall -Wextra -O2

# מטרת ברירת המחדל
all: $(TARGET)

# איך לקמפל את הקובץ הסופי
$(TARGET): $(OBJS)
	$(CXX) $(CXXFLAGS) -o $@ $^

# איך לקמפל כל קובץ .cpp לקובץ .o
%.o: %.cpp
	$(CXX) $(CXXFLAGS) -c $< -o $@

# ניקוי קבצים זמניים
clean:
	rm -f $(OBJS) $(TARGET)
