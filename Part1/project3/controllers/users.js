const Users = require ('../models/users');

// Get a user by ID
exports.getUser = (req, res) => {
  req.headers['user-id'];
  const id = req.params.id;
  const user = Users.getUser(id);
  if (user) {
    // User found
    res.status(200).json(user);
  } else {
    // User not found
    res.status(404).json({ error: "User not found" });
  }
};

exports.checkUsername = (req, res) => {
  const username = req.params.username;

  const existingUser = Users.getUserByUsername(username);
  if (existingUser) {
    return res.status(200).json({ isUnique: false });
  }

  return res.status(200).json({ isUnique: true });
}; 

exports.getUserByUsername = (username) => {
  return Users.users.find(user => user.username === username);
};

// Add a new user
exports.addUser = (req, res) => {
  
  const { mail, first_name, last_name, username, birth_date, gender, password, reWritePass } = req.body;
  let profileImage = req.file ? req.file.path : req.body.profileImage;

  // validating that all fields are provided
  if (!mail || !first_name || !last_name || !username || !birth_date || !gender || !password || !reWritePass) {
    return res.status(400).json({ error: "All fields are required" });
  }

  // validating that the email is in a valid format
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(mail)) {
    return res.status(400).json({ error: "Invalid email format" });
  }

  // valid that the password is at least 8 characters long and contains letters and numbers
  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/;
  if (!passwordRegex.test(password)) {
    return res.status(400).json({ error: "Password must be at least 8 characters long and contain letters and numbers" });
  }

  // validating that the passwords match
  if (password !== reWritePass) {
    return res.status(400).json({ error: "Passwords do not match" });
  }

  // validating first and last names (at least 2 letters, only letters)
  const nameRegex = /^[A-Za-zא-ת]{2,}$/;
  if (!nameRegex.test(first_name) || !nameRegex.test(last_name)) {
    return res.status(400).json({ error: "First and last names must contain at least 2 letters and only letters" });
  }

  // validdation for birth date
  if (isNaN(Date.parse(birth_date))) {
    return res.status(400).json({ error: "Invalid birth date format" });
  }

  // valid gender values
  const allowedGenders = ['male', 'female'];
  if (!allowedGenders.includes(gender.toLowerCase())) {
    return res.status(400).json({ error: "Invalid gender value" });
  }

  // cjeck if email already exists
  const existingUser = Users.getUserByMail(mail);
  if (existingUser) {
    return res.status(400).json({ error: "Email already exists" });
  }

  // check if username already exists
  const existingUserByUsername = Users.getUserByUsername(username); 
  if (existingUserByUsername) {
    return res.status(400).json({ error: "Username already exists" });
  }

// adding the user
  if (!profileImage) { // need to check if picture is provided ------?
    profileImage = "https://example.com/default-profile-picture.png"; // תמונה ברירת מחדל אם לא ניתנה
  }
  Users.addUser({ first_name, last_name, username, mail, birth_date, gender, password, reWritePass, profileImage });

  res.status(201).json({ message: "User added successfully" });
};

exports.updateUser = (req, res) => {
  const id = req.params.id;
  const { mail, first_name, last_name, username, birth_date, gender } = req.body;
  let profileImage = req.file ? req.file.path : req.body.profileImage;


  // Find the user
  const userIndex = Users.users.findIndex(user => user.id == id);
  if (userIndex === -1) {
    return res.status(404).json({ error: "User not found" });
  }

  const currentUser = Users.users[userIndex];

  // Validation for email format
  if (mail) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(mail)) {
      return res.status(400).json({ error: "Invalid email format" });
    }

    // Check if email already exists (but not for current user)
    const existingUser = Users.getUserByMail(mail);
    if (existingUser && existingUser.id != id) {
      return res.status(400).json({ error: "Email already exists" });
    }
  }

  // Validation for username
  if (username) {
    const existingUserByUsername = Users.getUserByUsername(username);
    if (existingUserByUsername && existingUserByUsername.id != id) {
      return res.status(400).json({ error: "Username already exists" });
    }
  }

  // Validation for names
  if (first_name || last_name) {
    const nameRegex = /^[A-Za-zא-ת]{2,}$/;
    if (first_name && !nameRegex.test(first_name)) {
      return res.status(400).json({ error: "First name must contain at least 2 letters and only letters" });
    }
    if (last_name && !nameRegex.test(last_name)) {
      return res.status(400).json({ error: "Last name must contain at least 2 letters and only letters" });
    }
  }

  // Validation for birth date
  if (birth_date && isNaN(Date.parse(birth_date))) {
    return res.status(400).json({ error: "Invalid birth date format" });
  }

  // Validation for gender
  if (gender) {
    const allowedGenders = ['male', 'female'];
    if (!allowedGenders.includes(gender.toLowerCase())) {
      return res.status(400).json({ error: "Invalid gender value" });
    }
  }

  // Update the user
  const updatedUser = {
    ...currentUser,
    ...(mail && { mail }),
    ...(first_name && { first_name }),
    ...(last_name && { last_name }),
    ...(username && { username }),
    ...(birth_date && { birth_date }),
    ...(gender && { gender }),
    ...(profileImage && { profileImage })
  };

  Users.users[userIndex] = updatedUser;

  // Return updated user without password
  const { password, reWritePass, ...userWithoutPassword } = updatedUser;
  res.status(200).json({ 
    message: "User updated successfully", 
    user: userWithoutPassword 
  });
};