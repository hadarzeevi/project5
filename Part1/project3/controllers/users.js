const User = require('../models/users');
const jwt = require('jsonwebtoken');
const SECRET_KEY = 'secretkeyHHE';


// Get a user by ID
exports.getUser = async (req, res) => {
  try {
    const id = req.params.id;
    const user = await User.findById(id);

    if (!user) {
      return res.status(404).json({ error: "User not found" });
    }

    res.status(200).json(user.toSafeObject());
  } catch (error) {
    console.error('Error fetching user:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
};

// Check if username is unique
exports.checkUsername = async (req, res) => {
  try {
    const username = req.params.username;
    const existingUser = await User.findByUsername(username);

    res.status(200).json({ isUnique: !existingUser });
  } catch (error) {
    console.error('Error checking username:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
};

// Get user by username (helper function)
exports.getUserByUsername = async (username) => {
  try {
    return await User.findByUsername(username);
  } catch (error) {
    console.error('Error getting user by username:', error);
    return null;
  }
};

// Get user by mail (helper function for tokens.js or login)
exports.getUserByMail = async (mail) => {
  try {
    return await User.findByEmail(mail);
  } catch (error) {
    console.error('Error getting user by mail:', error);
    return null;
  }
};

// Add a new user
exports.addUser = async (req, res) => {
  try {
    const { mail, first_name, last_name, username, birth_date, gender, password, reWritePass } = req.body;
    let profileImage = req.file ? req.file.path : req.body.profileImage;

    // Validation checks...
    if (!mail || !first_name || !last_name || !username || !birth_date || !gender || !password || !reWritePass) {
      return res.status(400).json({ error: "All fields are required" });
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(mail)) {
      return res.status(400).json({ error: "Invalid email format" });
    }

    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/;
    if (!passwordRegex.test(password)) {
      return res.status(400).json({ error: "Password must be at least 8 characters long and contain letters and numbers" });
    }

    if (password !== reWritePass) {
      return res.status(400).json({ error: "Passwords do not match" });
    }

    const nameRegex = /^[A-Za-zא-ת]{2,}$/;
    if (!nameRegex.test(first_name) || !nameRegex.test(last_name)) {
      return res.status(400).json({ error: "First and last names must contain at least 2 letters and only letters" });
    }

    if (isNaN(Date.parse(birth_date))) {
      return res.status(400).json({ error: "Invalid birth date format" });
    }

    const allowedGenders = ['male', 'female'];
    if (!allowedGenders.includes(gender.toLowerCase())) {
      return res.status(400).json({ error: "Invalid gender value" });
    }

    const existingUser = await User.findByEmail(mail);
    if (existingUser) {
      return res.status(400).json({ error: "Email already exists" });
    }

    const existingUserByUsername = await User.findByUsername(username);
    if (existingUserByUsername) {
      return res.status(400).json({ error: "Username already exists" });
    }

    if (!profileImage) {
      profileImage = "https://example.com/default-profile-picture.png";
    }

    const newUser = new User({
      first_name,
      last_name,
      username,
      mail,
      birth_date,
      gender,
      password,
      reWritePass,
      profileImage
    });

    await newUser.save();
    const token = jwt.sign({ userId: newUser._id }, SECRET_KEY, { expiresIn: '1h' });

    const fullProfileImageUrl = profileImage.startsWith('http')
      ? profileImage
      : `${req.protocol}://${req.get('host')}/${profileImage}`;

    res.status(201).json({
      message: "User added successfully",
      userId: newUser._id,
      token,
      profileImage: fullProfileImageUrl
    });
  } catch (error) {
    console.error('Error adding user:', error);
    if (error.name === 'ValidationError') {
      const messages = Object.values(error.errors).map(err => err.message);
      return res.status(400).json({ error: messages.join(', ') });
    }
    if (error.code === 11000) {
      const field = Object.keys(error.keyValue)[0];
      return res.status(400).json({ error: `${field} already exists` });
    }
    res.status(500).json({ error: 'Internal server error' });
  }
};

// Update user
exports.updateUser = async (req, res) => {
  try {
    const id = req.params.id;
    const { mail, first_name, last_name, username, birth_date, gender } = req.body;
    let profileImage = req.file ? req.file.path : req.body.profileImage;

    const currentUser = await User.findById(id);
    if (!currentUser) {
      return res.status(404).json({ error: "User not found" });
    }

    const updateData = {};

    if (mail) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(mail)) {
        return res.status(400).json({ error: "Invalid email format" });
      }
      const existingUser = await User.findByEmail(mail);
      if (existingUser && !existingUser._id.equals(id)) {
        return res.status(400).json({ error: "Email already exists" });
      }
      updateData.mail = mail;
    }

    if (username) {
      const existingUserByUsername = await User.findByUsername(username);
      if (existingUserByUsername && !existingUserByUsername._id.equals(id)) {
        return res.status(400).json({ error: "Username already exists" });
      }
      updateData.username = username;
    }

    const nameRegex = /^[A-Za-zא-ת]{2,}$/;
    if (first_name && !nameRegex.test(first_name)) {
      return res.status(400).json({ error: "First name must contain at least 2 letters and only letters" });
    }
    if (last_name && !nameRegex.test(last_name)) {
      return res.status(400).json({ error: "Last name must contain at least 2 letters and only letters" });
    }

    if (first_name) updateData.first_name = first_name;
    if (last_name) updateData.last_name = last_name;

    if (birth_date) {
      if (isNaN(Date.parse(birth_date))) {
        return res.status(400).json({ error: "Invalid birth date format" });
      }
      updateData.birth_date = birth_date;
    }

    if (gender) {
      const allowedGenders = ['male', 'female'];
      if (!allowedGenders.includes(gender.toLowerCase())) {
        return res.status(400).json({ error: "Invalid gender value" });
      }
      updateData.gender = gender;
    }

    if (profileImage) {
      updateData.profileImage = profileImage;
    }

    const updatedUser = await User.findByIdAndUpdate(id, updateData, {
      new: true,
      runValidators: true
    });

    res.status(200).json({
      message: "User updated successfully",
      user: updatedUser.toSafeObject()
    });
  } catch (error) {
    console.error('Error updating user:', error);
    if (error.name === 'ValidationError') {
      const messages = Object.values(error.errors).map(err => err.message);
      return res.status(400).json({ error: messages.join(', ') });
    }
    if (error.code === 11000) {
      const field = Object.keys(error.keyValue)[0];
      return res.status(400).json({ error: `${field} already exists` });
    }
    res.status(500).json({ error: 'Internal server error' });
  }
};
