const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  first_name: {
    type: String,
    required: true,
    minlength: 2,
    validate: {
      validator: function(v) {
        return /^[A-Za-zא-ת]{2,}$/.test(v);
      },
      message: 'First name must contain at least 2 letters and only letters'
    }
  },
  last_name: {
    type: String,
    required: true,
    minlength: 2,
    validate: {
      validator: function(v) {
        return /^[A-Za-zא-ת]{2,}$/.test(v);
      },
      message: 'Last name must contain at least 2 letters and only letters'
    }
  },
  username: {
    type: String,
    required: true,
    unique: true,
    trim: true
  },
  mail: {
    type: String,
    required: true,
    unique: true,
    lowercase: true,
    validate: {
      validator: function(v) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);
      },
      message: 'Invalid email format'
    }
  },
  birth_date: {
    type: Date,
    required: true
  },
  gender: {
    type: String,
    required: true,
    enum: ['male', 'female'],
    lowercase: true
  },
  password: {
    type: String,
    required: true,
    validate: {
      validator: function(v) {
        return /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/.test(v);
      },
      message: 'Password must be at least 8 characters long and contain letters and numbers'
    }
  },
  reWritePass: {
    type: String,
    required: true,
    validate: {
      validator: function(v) {
        return v === this.password;
      },
      message: 'Passwords do not match'
    }
  },
  profileImage: {
    type: String,
    default: "https://example.com/default-profile-picture.png"
  }
}, {
  timestamps: true
});

// מתודה להחזרת משתמש ללא סיסמה
userSchema.methods.toSafeObject = function() {
  const user = this.toObject();
  delete user.password;
  delete user.reWritePass;
  return user;
};

// מתודה סטטית לחיפוש משתמש לפי אימייל (case-insensitive)
userSchema.statics.findByEmail = function(email) {
  return this.findOne({ mail: new RegExp(`^${email}$`, 'i') });
};

// מתודה סטטית לחיפוש משתמש לפי שם משתמש (case-insensitive)
userSchema.statics.findByUsername = function(username) {
  return this.findOne({ username: new RegExp(`^${username}$`, 'i') });
};

// מתודה סטטית לאימות משתמש
userSchema.statics.authenticate = function(email, password) {
  return this.findOne({ mail: new RegExp(`^${email}$`, 'i'), password: password });
};

const User = mongoose.model('User', userSchema);

module.exports = User;
