import { useState, useEffect, useRef, useCallback } from 'react';

const API_BASE = '/api';

export default function App() {
  const [characters, setCharacters] = useState([]);
  const [selected, setSelected] = useState(null);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [streaming, setStreaming] = useState(false);
  const [currentStream, setCurrentStream] = useState('');
  const scrollRef = useRef(null);
  const inputRef = useRef(null);
  const abortRef = useRef(null);

  useEffect(() => {
    fetch(`${API_BASE}/characters`)
      .then(r => r.json())
      .then(setCharacters)
      .catch(console.error);
  }, []);

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'smooth' });
  }, [messages, currentStream]);

  const selectCharacter = (name) => {
    setSelected(name);
    setMessages([]);
    setCurrentStream('');
  };

  const sendMessage = useCallback(async () => {
    const text = input.trim();
    if (!text || !selected || streaming) return;

    setInput('');
    setMessages(prev => [...prev, { role: 'user', content: text }]);
    setStreaming(true);
    setCurrentStream('');

    const controller = new AbortController();
    abortRef.current = controller;

    try {
      const res = await fetch(`${API_BASE}/chat/${encodeURIComponent(selected)}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: text }),
        signal: controller.signal,
      });

      const reader = res.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';
      let fullResponse = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop();

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.substring(5).trim();
            if (data === '[DONE]') continue;
            fullResponse += data;
            setCurrentStream(fullResponse);
          }
        }
      }

      setMessages(prev => [...prev, { role: 'assistant', content: fullResponse }]);
    } catch (err) {
      if (err.name !== 'AbortError') {
        setMessages(prev => [...prev, { role: 'system', content: 'Connection error. Is the backend running?' }]);
      }
    } finally {
      setStreaming(false);
      setCurrentStream('');
      abortRef.current = null;
    }
  }, [input, selected, streaming]);

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const stopStreaming = () => {
    abortRef.current?.abort();
    if (currentStream) {
      setMessages(prev => [...prev, { role: 'assistant', content: currentStream }]);
    }
    setStreaming(false);
    setCurrentStream('');
  };

  if (!selected) {
    return (
      <div className="terminal-container">
        <div className="terminal-header">LEARNING SCRIBE</div>
        <div className="character-select">
          <h2>Select a character:</h2>
          {characters.map(c => (
            <button key={c.name} className="char-btn" onClick={() => selectCharacter(c.name)}>
              <span className="char-name">{c.name}</span>
              <span className="char-intro">{c.introduction}</span>
            </button>
          ))}
        </div>
      </div>
    );
  }

  const currentChar = characters.find(c => c.name === selected);

  return (
    <div className="terminal-container" onClick={() => inputRef.current?.focus()}>
      <div className="terminal-header">
        <span>{selected}</span>
        <button className="back-btn" onClick={() => { setSelected(null); setMessages([]); }}>← Back</button>
      </div>

      <div className="terminal-output" ref={scrollRef}>
        {currentChar && (
          <div className="message system">
            <span className="prefix">SYSTEM:</span> {currentChar.introduction}
          </div>
        )}

        {messages.map((msg, i) => (
          <div key={i} className={`message ${msg.role}`}>
            <span className="prefix">
              {msg.role === 'user' ? 'YOU:' : msg.role === 'assistant' ? `${selected.toUpperCase()}:` : 'SYSTEM:'}
            </span>{' '}
            {msg.content}
          </div>
        ))}

        {streaming && currentStream && (
          <div className="message assistant streaming">
            <span className="prefix">{selected.toUpperCase()}:</span>{' '}
            {currentStream}
            <span className="cursor">▌</span>
          </div>
        )}
      </div>

      <div className="terminal-input-area">
        <span className="prompt">YOU: </span>
        <input
          ref={inputRef}
          className="terminal-input"
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder={streaming ? 'Waiting for response...' : 'Type your message...'}
          disabled={streaming}
          autoFocus
        />
        {streaming ? (
          <button className="send-btn stop" onClick={stopStreaming}>■ Stop</button>
        ) : (
          <button className="send-btn" onClick={sendMessage} disabled={!input.trim()}>↵ Send</button>
        )}
      </div>
    </div>
  );
}
