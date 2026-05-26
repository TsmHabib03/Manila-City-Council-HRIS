const puppeteer = require('puppeteer');
const path = require('path');
const fs = require('fs');

const BASE = 'http://localhost:8080/hrisp';
const OUT  = 'C:\\Users\\Habib\\IdeaProjects\\hrisp-web01\\uat_screenshots_employee';

const PAGES = [
  { url: '/change-password/4',                                             name: 'emp_CPW-01.jpeg'   },
  { url: '/employee/familybg/4/PROFILE/56ec21099f5f4879b190',              name: 'emp_PDS-02.jpeg'   },
  { url: '/employee/educationalbg/4/PROFILE/56ec21099f5f4879b190',         name: 'emp_PDS-03.jpeg'   },
  { url: '/employee/civil-eligibility/4/PROFILE/56ec21099f5f4879b190',     name: 'emp_PDS-04.jpeg'   },
  { url: '/employee/work-experience/4/PROFILE/56ec21099f5f4879b190',       name: 'emp_PDS-05.jpeg'   },
  { url: '/employee/voluntary-work/4/PROFILE/56ec21099f5f4879b190',        name: 'emp_PDS-06.jpeg'   },
  { url: '/employee/learning-development/4/PROFILE/56ec21099f5f4879b190',  name: 'emp_PDS-07.jpeg'   },
  { url: '/employee/other-info/4/PROFILE/56ec21099f5f4879b190',            name: 'emp_PDS-08.jpeg'   },
  { url: '/employee/references/4/PROFILE/56ec21099f5f4879b190',            name: 'emp_PDS-09.jpeg'   },
  { url: '/employee/government-id/4/PROFILE/56ec21099f5f4879b190',         name: 'emp_PDS-10.jpeg'   },
  { url: '/my-appointments/4/56ec21099f5f4879b190',                        name: 'emp_APT-01.jpeg'   },
  { url: '/my-appointments/4/56ec21099f5f4879b190',                        name: 'emp_APT-LIST.jpeg' },
  { url: '/myclearance/4/56ec21099f5f4879b190',                            name: 'emp_CLR-01.jpeg'   },
  { url: '/myclearance/4/56ec21099f5f4879b190',                            name: 'emp_CLR-02.jpeg'   },
  { url: '/my201files/4/56ec21099f5f4879b190',                             name: 'emp_F201-01.jpeg'  },
  { url: '/my-service-record/4/56ec21099f5f4879b190',                      name: 'emp_SVC-01.jpeg'   },
];

async function delay(ms) { return new Promise(r => setTimeout(r, ms)); }

async function main() {
  if (!fs.existsSync(OUT)) fs.mkdirSync(OUT, { recursive: true });

  console.log('Launching browser...');
  const browser = await puppeteer.launch({
    headless: true,
    executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--window-size=1400,900']
  });

  const page = await browser.newPage();
  await page.setViewport({ width: 1400, height: 900 });

  // Login
  console.log('Logging in as sabao02111965...');
  await page.goto(BASE + '/login', { waitUntil: 'networkidle2' });
  await page.type('input[name="username"]', 'sabao02111965');
  await page.type('input[name="password"]', '123');
  await Promise.all([
    page.waitForNavigation({ waitUntil: 'networkidle2' }),
    page.click('button[type="submit"], input[type="submit"]')
  ]);
  console.log('Logged in! Current URL:', page.url());

  for (const p of PAGES) {
    console.log(`\nCapturing: ${p.name} ...`);
    await page.goto(BASE + p.url, { waitUntil: 'networkidle2', timeout: 30000 });
    await delay(1500);

    const outPath = path.join(OUT, p.name);
    await page.screenshot({ path: outPath, type: 'jpeg', quality: 88, fullPage: true });
    const size = fs.statSync(outPath).size;
    console.log(`  Saved: ${outPath} (${Math.round(size/1024)}KB)`);
  }

  await browser.close();
  console.log('\nAll done! Screenshots saved to:', OUT);
}

main().catch(err => {
  console.error('ERROR:', err.message);
  process.exit(1);
});
