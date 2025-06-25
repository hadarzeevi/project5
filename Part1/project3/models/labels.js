
let labels = [];
// Returns all labels - filtered by userId
const getAllLabels = (userId) => {

    return labels.filter(label => label.userId === userId);
};

// Adds a new label to the array
const addLabel = (label) => {
    labels.push(label);
}

// Finds a label by its ID
const getLabel = (id) => {
    return labels.find((label) => label.id == id);
};

// Updates a label by ID
const editLabel = (id, updatedLabel) => {
    const index = labels.findIndex(labels => labels.id == id);
    if (index !== -1) {
        labels[index] = updatedLabel;
    }
};

// Deletes a label by ID
const deleteLabel = (id) => {
  const index = labels.findIndex(label => label.id == id);
  if (index !== -1) {
    labels.splice(index, 1); // deletes the label from the array
  }
};

module.exports = {
    labels,
    addLabel,
    getAllLabels,
    getLabel,
    editLabel,
    deleteLabel
};
