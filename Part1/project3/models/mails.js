let emails = [];

// Returns an email by its ID
const getMail = (id) => emails.find(mail => mail.id == id);

// Adds a new email to the array
const addMail = (mail) => emails.push(mail);

// Updates an existing email by ID
const patchMail = (id, updatedMail) => {
    const index = emails.findIndex(mail => mail.id == id);
    if (index !== -1) {
        emails[index] = updatedMail;
    }
};

// Deletes an email by ID
const deleteMail = (id) => {
  const index = emails.findIndex(mail => mail.id == id);
  if (index !== -1) {
    emails.splice(index, 1); // deletes the mail from the array
  }
};

// Searches emails by a text query in subject, body, or from fields
function search(query) {
  return emails.filter(mail =>
    (typeof mail.subject === 'string' && mail.subject.includes(query)) ||
    (typeof mail.body === 'string' && mail.body.includes(query)) ||
    (typeof mail.from === 'string' && mail.from.includes(query))
  );
}

module.exports = { 
    emails,
    getMail,
    patchMail,
    addMail,
    deleteMail,
    search,
    
};
