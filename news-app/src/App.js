import { useState, useEffect } from 'react';
import './App.css';

function App() {
  const [news, setNews] = useState([]);
  const [keyword, setKeyword] = useState('криптовалюта');
  const [selectedLanguage, setSelectedLanguage] = useState('ru');
  const [selectedSortBy, setSelectedSortBy] = useState('publishedAt');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const languages = [
      { code: 'ar', name: 'Arabic' },
      { code: 'de', name: 'German' },
      { code: 'en', name: 'English' },
      { code: 'es', name: 'Spanish' },
      { code: 'fr', name: 'French' },
      { code: 'he', name: 'Hebrew' },
      { code: 'it', name: 'Italian' },
      { code: 'nl', name: 'Dutch' },
      { code: 'no', name: 'Norwegian' },
      { code: 'pt', name: 'Portuguese' },
      { code: 'ru', name: 'Русский' },
      { code: 'sv', name: 'Swedish' },
      { code: 'ud', name: 'Urdu' },
      { code: 'zh', name: 'Chinese' },
    ];

  const sortOptions = [
      { value: 'relevancy', name: 'По релевантности' },
      { value: 'popularity', name: 'По популярности' },
      { value: 'publishedAt', name: 'По дате публикации' },
    ];

  const fetchNews = async () => {
    try {
      setLoading(true);
      setError(null);
      console.log('Sending request for keyword:', keyword, 'language:', selectedLanguage, 'sortBy:', selectedSortBy);

      const queryParams = new URLSearchParams({
              keyword: keyword,
              language: selectedLanguage,
              sortBy: selectedSortBy
            }).toString();

      const response = await fetch(`/api/news?keyword=${keyword}&language=ru&sortBy=publishedAt&apiKey=…`);
      console.log('Response status:', response.status);

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP error! Status: ${response.status}`);
      }

      const data = await response.json();
      console.log('Received data:', data);

      if (!Array.isArray(data)) {
              // News API может вернуть пустой массив, а не null, если статей нет
              // или если ответ является объектом с ошибкой
              if (data && data.status === "error") {
                  throw new Error(`News API Error: ${data.message}`);
              }
              throw new Error('Invalid data format: expected array of articles');
      }

      setNews(data);
    } catch (err) {
      console.error('Fetch error:', err);
      setError(err.message);
      setNews([]); // Очищаем предыдущие новости при ошибке
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
      const timer = setTimeout(() => {
        if (keyword.trim()) { // Запрос только если ключевое слово не пустое
          fetchNews();
        } else {
          setNews([]); // Очистить новости, если ключевое слово пустое
        }
      }, 500);

      return () => clearTimeout(timer);
    }, [keyword, selectedLanguage, selectedSortBy]);

  return (
    <div className="App">
          <h1>Новости</h1>
          <div className="controls">
            <input
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="Введите тему"
              className="search-input"
          />

      <select
                value={selectedLanguage}
                onChange={(e) => setSelectedLanguage(e.target.value)}
                className="language-select"
                title="Выберите язык новостей"
              >
                {languages.map((lang) => (
                  <option key={lang.code} value={lang.code}>
                    {lang.name}
                  </option>
                ))}
              </select>

              <select
                value={selectedSortBy}
                onChange={(e) => setSelectedSortBy(e.target.value)}
                className="sort-by-select"
                title="Сортировать по"
              >
                {sortOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.name}
                  </option>
                ))}
              </select>
            </div>

      {loading && <div className="loading">Загрузка...</div>}

      {error && (
        <div className="error">
          Ошибка: {error}
          <button onClick={fetchNews}>Повторить</button>
        </div>
      )}
      {!loading && !error && news.length === 0 && keyword.trim() && (
              <div className="no-results">По вашему запросу "{keyword}" не найдено новостей на языке {selectedLanguage.toUpperCase()}.</div>
      )}

      <div className="news-grid">
              {news.map((article, index) => (
                <div key={index} className="news-card">
                  <h3>{article.title}</h3>
                  <p>{article.description || 'Нет описания'}</p>
                  <a
                    href={article.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="read-more"
                  >
                    Читать полностью →
                  </a>
                </div>
              ))}
            </div>
          </div>
  );
}

export default App;