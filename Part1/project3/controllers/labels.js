const mongoose = require('mongoose');
const Label = require('../models/labels.js');

// Get all labels for a specific user
exports.getAllLabels = async (req, res) => {
  try {
    const userIdStr = req.user.userId;

    if (!mongoose.Types.ObjectId.isValid(userIdStr)) {
      return res.status(400).json({ error: "Invalid user ID" });
    }

    const userId = new mongoose.Types.ObjectId(userIdStr);
    const userLabels = await Label.find({ userId });

    res.json(userLabels);
  } catch (error) {
    console.error('Error in getAllLabels:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
};

// Get a single label by ID
exports.getLabel = async (req, res) => {
  try {
    const id = req.params.id;
    const label = await Label.findById(id);

    if (!label) {
      return res.status(404).json({ error: "Label not found" });
    }

    if (!label.userId.equals(req.user.userId)) {
      return res.status(403).json({ error: "Access denied" });
    }

    res.status(200).json(label);
  } catch (error) {
    console.error('Error in getLabel:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
};

// Edit label fields
exports.editLabel = async (req, res) => {
  try {
    const id = req.params.id;
    const label = await Label.findById(id);

    if (!label) {
      return res.status(404).json({ error: "Label not found" });
    }

    if (!label.userId.equals(req.user.userId)) {
      return res.status(403).json({ error: "Access denied" });
    }

    const allowedFields = ['name', 'color'];
    const updateData = {};

    for (const field of allowedFields) {
      if (req.body[field] !== undefined) {
        updateData[field] = req.body[field];
      }
    }

    await Label.findByIdAndUpdate(id, updateData, { runValidators: true });
    res.status(204).json();
  } catch (error) {
    console.error('Error in editLabel:', error);
    if (error.code === 11000) {
      return res.status(409).json({ error: 'Label name already exists for this user' });
    }
    res.status(500).json({ error: 'Internal server error' });
  }
};

// Create a new label
exports.addLabel = async (req, res) => {
  try {
    const userIdStr = req.user.userId;
    const { name, color } = req.body;

    if (!name || !mongoose.Types.ObjectId.isValid(userIdStr)) {
      return res.status(400).json({ error: 'Name and valid user ID are required' });
    }

    const userId = new mongoose.Types.ObjectId(userIdStr);

    const newLabel = new Label({
      name,
      color,
      userId
    });

    const savedLabel = await newLabel.save();
    res.status(201).location(`/api/labels/${savedLabel._id}`).json(savedLabel);
  } catch (error) {
    console.error('Error in addLabel:', error);
    if (error.code === 11000) {
      return res.status(409).json({ error: 'Label name already exists for this user' });
    }
    res.status(500).json({ error: 'Internal server error' });
  }
};

// Delete label by ID
exports.deleteLabel = async (req, res) => {
  try {
    const id = req.params.id;
    const label = await Label.findById(id);

    if (!label) {
      return res.status(404).json({ error: "Label not found" });
    }

    if (!label.userId.equals(req.user.userId)) {
      return res.status(403).json({ error: "Access denied" });
    }

    await Label.findByIdAndDelete(id);
    res.status(204).json();
  } catch (error) {
    console.error('Error in deleteLabel:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
};
