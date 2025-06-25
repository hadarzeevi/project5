const net = require('net');
const PORT = 8080;
const HOST = 'blacklist'; // or the IP address of your blacklist server

let client = null;
let queue = [];
let buffer = '';
let ready = false;

// Initialize TCP connection to the blacklist server
const initConnection = () => {
  return new Promise((resolve, reject) => {
    client = new net.Socket();
    client.connect(PORT, HOST, () => {
    });

    client.on('data', (data) => {
      buffer += data.toString();

      // cehck if we have a complete response
      if (buffer.includes('\n\n')) {
        const response = buffer.trim();
        buffer = '';

        const resolveFunc = queue.shift(); // will get the first function in the queue
        if (resolveFunc) resolveFunc(response);
      }
    });

    client.on('error', (err) => {
      console.error('[ERROR] Socket error:', err.message);
      reject(err);
    });

    client.on('close', () => {
      console.log('[INFO] Socket closed');
      client = null;
    });

// Wait for the initial configuration response
    // once we get the first response, we can consider the connection ready
    // and we can start processing commands
    client.once('data', (firstResponse) => {
      const res = firstResponse.toString();
      if (res.includes('Configuration accepted') || res.includes('Ready for commands.')) {
        ready = true;
        buffer = ''; // reset buffer after initial configuration
        resolve();
      } else {
        reject(new Error('Unexpected configuration response: ' + res));
      }
    });
  });
};

const sendToServer = async (command, url) => {

  if (!client || !ready) {
    await initConnection();
  }

  return new Promise((resolve, reject) => {
    queue.push(resolve);  // add the resolve function to the queue to call it later

    const commandLine = `${command} ${url}\n`;
    client.write(commandLine);
  });
};

const extractUrls = (text) => {
    const urlRegex = /\b(?:https?:\/\/|www\.)[^\s]+/g;
    return text.match(urlRegex) || [];
};

const check = async (subject, body) => {
  try {
        const urls = [...extractUrls(subject), ...extractUrls(body)];
    if (urls.length === 0) return false;

    for (const url of urls) {
      const response = await sendToServer('GET', url);
      let bloom, exact;
        const parts = response.split('\n\n');
        if (parts.length === 2) {
          [bloom, exact] = parts[1].split(' ');
        } else {
          bloom = response.trim();
          exact = undefined;
        }

        if (bloom === 'true' && (exact === 'true' || exact === undefined)) {
          return true;  // block the email if any link is blocked
        }

      if (bloom === 'true' && (exact === 'true' || exact === undefined)) {
        return true;
      }
    }
    return false;
  } catch (error) {
    console.error('[ERROR] Failed to check blacklist:', error);
    // כאן מחזירים false או true לפי ההחלטה שלך, לדוגמה false כדי לא לחסום בשגיאה
    return false;
  }
};

// Add a URL to the blacklist
const add = async (url) => {
  const result = await sendToServer('POST', url);
  return result;
};

// Remove a URL from the blacklist
const remove = async (url) => {
  const result = await sendToServer('DELETE', url);
  return result;
};


module.exports = {
    sendToServer,
    extractUrls, 
    check,
    add,
    remove
};















