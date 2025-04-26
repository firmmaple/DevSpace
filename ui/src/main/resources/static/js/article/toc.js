/**
 * Table of Contents Generator
 * 
 * This script generates a table of contents for article pages based on headings (h1-h6)
 * in the article content. It also supports highlighting the active section during scrolling.
 */

// Store heading elements and their TOC links for scroll tracking
const headingElements = [];
const tocLinks = [];

// Generate a unique ID for headings that don't have one
function generateId(text) {
    return text
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/(^-|-$)/g, '');
}

// Create the table of contents
function generateTableOfContents() {
    const articleContent = document.getElementById('article-content');
    const tocContainer = document.getElementById('toc-container');
    const tocCard = document.getElementById('table-of-contents');
    const tocLoading = document.getElementById('toc-loading');
    const tocEmpty = document.getElementById('toc-empty');
    
    // Make sure elements exist
    if (!articleContent || !tocContainer || !tocCard) return;
    
    // Add sticky class to the TOC card
    tocCard.classList.add('toc-sticky');
    
    // Find all headings in the article
    const headings = articleContent.querySelectorAll('h1, h2, h3, h4, h5, h6');
    
    // Hide loading indicator
    if (tocLoading) {
        tocLoading.classList.add('d-none');
    }
    
    // If no headings, show empty message and return
    if (headings.length === 0) {
        if (tocEmpty) {
            tocEmpty.classList.remove('d-none');
        }
        tocCard.classList.remove('d-none');
        return;
    }
    
    // Process each heading
    headings.forEach((heading, index) => {
        // Get or create ID for the heading (needed for anchor links)
        if (!heading.id) {
            heading.id = generateId(heading.textContent) || `section-${index}`;
        }
        
        // Create TOC entry
        const tocEntry = document.createElement('a');
        tocEntry.href = `#${heading.id}`;
        tocEntry.className = `toc-link toc-${heading.tagName.toLowerCase()}`;
        tocEntry.textContent = heading.textContent;
        tocEntry.setAttribute('data-target', heading.id);
        
        // Add click event to scroll smoothly
        tocEntry.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Get the target element
            const targetId = this.getAttribute('data-target');
            const targetElement = document.getElementById(targetId);
            
            if (targetElement) {
                // Scroll the target into view with smooth behavior
                targetElement.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
                
                // Update URL hash without jumping
                history.pushState(null, null, `#${targetId}`);
                
                // Update active TOC item
                updateActiveTocItem(targetId);
            }
        });
        
        // Store references for scroll tracking
        headingElements.push(heading);
        tocLinks.push(tocEntry);
        
        // Add to TOC container
        tocContainer.appendChild(tocEntry);
    });
    
    // Show the TOC card
    tocCard.classList.remove('d-none');
    
    // Set up toggle functionality
    setupTocToggle();
    
    // Set up scroll listener for highlighting active section
    setupScrollListener();
}

// Set up toggle functionality for the TOC
function setupTocToggle() {
    const toggleButton = document.getElementById('toc-toggle');
    const tocContainer = document.getElementById('toc-container');
    
    if (!toggleButton || !tocContainer) return;
    
    // Add click event to toggle button
    toggleButton.addEventListener('click', function() {
        // Toggle collapsed state of TOC container
        tocContainer.classList.toggle('collapsed');
        
        // Toggle the button appearance
        this.classList.toggle('collapsed');
        
        // Update aria-expanded attribute
        const isExpanded = !tocContainer.classList.contains('collapsed');
        this.setAttribute('aria-expanded', isExpanded);
        
        // Change icon direction based on collapsed state
        const icon = this.querySelector('i');
        if (icon) {
            if (isExpanded) {
                icon.className = 'fas fa-chevron-down';
            } else {
                icon.className = 'fas fa-chevron-right';
            }
        }
    });
}

// Highlight the TOC entry for the current section being viewed
function updateActiveTocItem(activeId) {
    // Remove active class from all TOC links
    tocLinks.forEach(link => {
        link.classList.remove('active');
    });
    
    // Add active class to the matching TOC link
    if (activeId) {
        const activeLink = tocLinks.find(link => link.getAttribute('data-target') === activeId);
        if (activeLink) {
            activeLink.classList.add('active');
            
            // No need to scroll to active item since we're displaying the full TOC without scrolling
        }
    }
}

// Set up scroll listener to update active TOC item during scrolling
function setupScrollListener() {
    if (headingElements.length === 0) return;
    
    window.addEventListener('scroll', debounce(function() {
        // Get current scroll position
        const scrollPosition = window.scrollY + 100; // Offset to trigger earlier
        
        // Find the current section
        let currentSection = null;
        
        // Loop backwards to find the last heading that's above current scroll position
        for (let i = headingElements.length - 1; i >= 0; i--) {
            const heading = headingElements[i];
            if (heading.offsetTop <= scrollPosition) {
                currentSection = heading.id;
                break;
            }
        }
        
        // If no section found and we have headings, use the first one
        if (!currentSection && headingElements.length > 0) {
            currentSection = headingElements[0].id;
        }
        
        // Update active TOC item
        updateActiveTocItem(currentSection);
    }, 100));
}

// Debounce helper function to limit scroll event handling
function debounce(func, wait) {
    let timeout;
    return function() {
        const context = this;
        const args = arguments;
        clearTimeout(timeout);
        timeout = setTimeout(() => {
            func.apply(context, args);
        }, wait);
    };
}

// Initialize TOC when the article content is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Wait for article data to be loaded
    const checkArticleLoaded = setInterval(function() {
        const articleContainer = document.getElementById('article-container');
        if (articleContainer && !articleContainer.classList.contains('d-none')) {
            clearInterval(checkArticleLoaded);
            generateTableOfContents();
        }
    }, 100);
    
    // Clear the interval after 10 seconds to prevent infinite checking
    setTimeout(function() {
        clearInterval(checkArticleLoaded);
    }, 10000);
}); 