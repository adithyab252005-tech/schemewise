import requests
from bs4 import BeautifulSoup
from urllib.parse import urljoin, urlparse
import urllib3

# Suppress SSL warnings
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

class DiscoveryAgent:
    def __init__(self, start_urls: list):
        self.start_urls = start_urls
        self.visited = set()

    def crawl(self, limit=10):
        """
        Simple crawler to find relevant scheme URLs.
        Returns a list of potential scheme URLs.
        """
        found_urls = []
        queue = self.start_urls.copy()
        
        while queue and len(found_urls) < limit:
            url = queue.pop(0)
            if url in self.visited:
                continue
            
            self.visited.add(url)
            print(f"Crawling: {url}")
            
            try:
                # Many gov sites have SSL issues, so we disable verification
                response = requests.get(url, timeout=10, verify=False)
                if response.status_code != 200:
                    print(f"Failed to access {url}: Status {response.status_code}")
                    continue
                
                soup = BeautifulSoup(response.text, 'html.parser')
                
                # Heuristic: Check if page title or content mentions "Scheme", "Yojna", "Eligibility"
                page_text = soup.get_text().lower()
                
                # Check for scheme-like content
                if ("scheme" in page_text or "yojna" in page_text or "benefit" in page_text) and len(page_text) > 500:
                    found_urls.append(url)

                # Extract links
                for link in soup.find_all('a', href=True):
                    full_url = urljoin(url, link['href'])
                    parsed_url = urlparse(full_url)
                    
                    # Filter for gov.in and nic.in
                    if parsed_url.netloc.endswith('.gov.in') or parsed_url.netloc.endswith('.nic.in'):
                        # Exclude structural/unnecessary pages
                        lower_url = full_url.lower()
                        exclusion_keywords = [
                            '/about', '/contact', 'faq', 'disclaimer', 'terms',
                            'help', 'login', 'register', 'calendar', 'news',
                            'report', 'gallery', 'vacancy', 'vacancies', 'event',
                            'directory', 'admin', 'dashboard', 'sitemap', 'feedback',
                            'tender', 'press', 'media', '/pdf/', 'download', 'archive'
                        ]
                        
                        if lower_url.endswith(('.pdf', '.jpg', '.png', '.xls', '.xlsx', '.doc', '.docx')):
                            continue
                            
                        # If the URL contains any exclusion keywords, skip it entirely
                        if any(kw in lower_url for kw in exclusion_keywords):
                            continue
                            
                        if full_url not in self.visited:
                            queue.append(full_url)

            except Exception as e:
                print(f"Error crawling {url}: {e}")
                
        return found_urls

    def fetch_content(self, url: str) -> str:
        """
        Fetches the text content of a URL.
        """
        try:
            # Disable SSL verification for content fetching too
            response = requests.get(url, timeout=10, verify=False)
            soup = BeautifulSoup(response.text, 'html.parser')
            # remove scripts and styles
            for script in soup(["script", "style"]):
                script.extract()
            text = soup.get_text()
            # break into lines and remove leading and trailing space on each
            lines = (line.strip() for line in text.splitlines())
            # break multi-headlines into a line each
            chunks = (phrase.strip() for line in lines for phrase in line.split("  "))
            # drop blank lines
            text = '\n'.join(chunk for chunk in chunks if chunk)
            return text
        except Exception as e:
            print(f"Error fetching content from {url}: {e}")
            return ""
