let nextId = 1;

const users = [];

// Returns user by ID (without password)
const getUser = (id) => {
  const user =  users.find((user) => user.id == id);
  if (!user) return undefined; 
  const { password, ...userWithoutPassword } = user;
  return userWithoutPassword;
};

// Returns user ID by email, or null if not found
const getUserIDByEmail = (email) => {
  const user = users.find(u => u.mail === email);
  if (!user) {
    return null; 
  }
  return user.id;
};

const getUserByUsername = (username) => {
  return users.find(user =>
    user.username.localeCompare(username, undefined, { sensitivity: 'base' }) === 0
  );
};

// Adds a new user and assigns a unique ID
const addUser = (user) => {
  user.id = nextId++;
  users.push(user);
};

// Authenticates user by email and password
const findUser = (mail, password) => {
  return users.find((u) =>
    u.mail === mail &&
    u.password === password
  );
};

// Returns user by email (case-insensitive)
const getUserByMail = (mail) => {
  return users.find(user =>
    user.mail.localeCompare(mail, undefined, { sensitivity: 'base' }) === 0
  );
};

module.exports = {
  users,
  getUser,
  addUser,
  findUser,
  getUserIDByEmail,
  getUserByMail,
  getUserByUsername
};
