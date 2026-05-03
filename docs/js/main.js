function copyCode(btn) {
    const pre = btn.parentElement.nextElementSibling;
    const code = pre.querySelector('code').innerText;
    
    navigator.clipboard.writeText(code).then(() => {
        const originalText = btn.innerHTML;
        btn.innerHTML = 'Copied! <i class="fa-solid fa-check"></i>';
        btn.classList.add('active');
        
        setTimeout(() => {
            btn.innerHTML = originalText;
            btn.classList.remove('active');
        }, 2000);
    });
}

// Sidebar Scroll Sync
document.addEventListener('DOMContentLoaded', () => {
    const sidebar = document.querySelector('.sidebar');
    const activeLink = document.querySelector('.nav-link.active');

    if (activeLink && sidebar) {
        const linkOffset = activeLink.offsetTop;
        const sidebarHeight = sidebar.clientHeight;
        if (linkOffset > sidebarHeight) {
            sidebar.scrollTop = linkOffset - (sidebarHeight / 2);
        }
    }

    // Page Transition Effect
    document.body.style.opacity = '0';
    setTimeout(() => {
        document.body.style.transition = 'opacity 0.4s ease';
        document.body.style.opacity = '1';
    }, 50);

    // Lightbox System
    const images = Array.from(document.querySelectorAll('.demo-image-wrapper img'));
    let currentIndex = 0;

    if (images.length > 0) {
        // Inject Lightbox HTML
        const lb = document.createElement('div');
        lb.className = 'lightbox';
        lb.innerHTML = `
            <div class="lightbox-content">
                <span class="lightbox-close"><i class="fa-solid fa-xmark"></i></span>
                <img class="lightbox-img" src="" alt="">
                <p class="lightbox-caption"></p>
                <div class="lightbox-nav">
                    <button class="lightbox-btn prev"><i class="fa-solid fa-chevron-left"></i></button>
                    <button class="lightbox-btn next"><i class="fa-solid fa-chevron-right"></i></button>
                </div>
            </div>
        `;
        document.body.appendChild(lb);

        const lbImg = lb.querySelector('.lightbox-img');
        const lbCap = lb.querySelector('.lightbox-caption');
        const lbClose = lb.querySelector('.lightbox-close');
        const lbPrev = lb.querySelector('.prev');
        const lbNext = lb.querySelector('.next');

        const openLightbox = (index) => {
            currentIndex = index;
            const img = images[currentIndex];
            lbImg.src = img.src;
            lbCap.innerText = img.nextElementSibling?.innerText || '';
            lb.classList.add('active');
            document.body.style.overflow = 'hidden';
        };

        const closeLightbox = () => {
            lb.classList.remove('active');
            document.body.style.overflow = '';
        };

        const nextImg = () => openLightbox((currentIndex + 1) % images.length);
        const prevImg = () => openLightbox((currentIndex - 1 + images.length) % images.length);

        images.forEach((img, i) => {
            img.addEventListener('click', () => openLightbox(i));
        });

        lbClose.addEventListener('click', closeLightbox);
        lb.addEventListener('click', (e) => { if (e.target === lb) closeLightbox(); });
        lbNext.addEventListener('click', (e) => { e.stopPropagation(); nextImg(); });
        lbPrev.addEventListener('click', (e) => { e.stopPropagation(); prevImg(); });

        document.addEventListener('keydown', (e) => {
            if (!lb.classList.contains('active')) return;
            if (e.key === 'Escape') closeLightbox();
            if (e.key === 'ArrowRight') nextImg();
            if (e.key === 'ArrowLeft') prevImg();
        });
    }
});
