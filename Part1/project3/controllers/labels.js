const labelsModel = require('../models/labels');

// Return all labels (even if empty) - filtered by userId
exports.getAllLabels = (req, res) => {
  console.log('GET /api/labels called, userId:', req.user.userId);
  const userId = req.user.userId; // using user ID from authentication middleware (token)
  if (isNaN(userId)) {
    return res.status(400).json({ error: "Missing or invalid user ID" });
  } //No longer needed, as userId is set by auth middleware
  const userLabels = labelsModel.getAllLabels(userId);

  res.json(userLabels);



  // res.status(200).json(userLabels); 
};


// Return a specific label by ID
exports.getLabel = (req, res) => {
  const id = req.params.id;
  const label = labelsModel.getLabel(id);

  if (label) {
    if (label.userId !== req.user.userId) {
      return res.status(403).json({ error: "Access denied" });
    }
      res.status(200).json(label);
  } else {
      res.status(404).json({ error: "Label not found" });
  }
};

// Edit label fields (name or color)
exports.editLabel = (req, res) => {
  //header
  const id = req.params.id;
  const label = labelsModel.getLabel(id); 

  if (!label) {
    return res.status(404).json({ error: "Label not found" });
  }
  if (label.userId !== req.user.userId) {
      return res.status(403).json({ error: "Access denied" });
  }
  // Update allowed fields only const allowedFields = ['name', 'color']; ----??
  for (const field of allowedFields) {
    if (req.body[field] !== undefined) {
      label[field] = req.body[field];
    }
  }
  //save changes
  labelsModel.editLabel(id, label); 
  res.status(204).json();
};
// Create a new label
exports.addLabel = (req, res) => {
  const userId = Number(req.user.userId); // using user ID from authentication middleware (token
  const { name, color } = req.body;

  if (!name || isNaN(userId)) {
    return res.status(400).json({ error: 'Name and valid user ID are required' });
  }

  const existingLabel = labelsModel.labels.find(
    (label) => label.userId === userId && label.name.toLowerCase() === name.toLowerCase()
  );

  if (existingLabel) {
    return res.status(409).json({ error: 'Label name already exists for this user' });
  }

  const newLabel = {
    id: labelsModel.labels.length > 0 ? labelsModel.labels[labelsModel.labels.length - 1].id + 1 : 1,
    name,
    color,
    userId
  };

  labelsModel.addLabel(newLabel);
  res.status(201).location(`/api/labels/${newLabel.id}`).json(newLabel);
};


// Delete label by ID
exports.deleteLabel = (req, res) => {
  //header
  const id = req.params.id;
  const label = labelsModel.getLabel(id); 

  if (!label) {
    return res.status(404).json({ error: "label not found" });
  }
  if (label.userId !== req.user.userId) {
      return res.status(403).json({ error: "Access denied" });
  }
  //save changes
  labelsModel.deleteLabel(id); 
  res.status(204).json();
};
