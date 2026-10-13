-- Optional demo job listings. The application also has an APP_DEMO_DATA
-- initializer that seeds the same listings and demo accounts on an empty DB.
USE career_compass;

INSERT INTO jobs
    (id, title, company, description, location, salary, experience_required, created_at)
VALUES
    ('demo-java-backend', 'Java Backend Engineer', 'Northstar Labs',
     'Build reliable APIs and distributed services for our product platform. Work with Java, Spring Boot, REST APIs, MySQL, Docker and cloud deployments.',
     'Bengaluru, India', 1200000, 2, CURRENT_TIMESTAMP),
    ('demo-full-stack', 'Full Stack Developer', 'Olive Systems',
     'Ship customer-facing applications across the stack. Build React interfaces and Java Spring Boot services backed by PostgreSQL and automated tests.',
     'Remote, India', 1450000, 2, CURRENT_TIMESTAMP),
    ('demo-ml-associate', 'Machine Learning Associate', 'Lattice AI',
     'Prepare datasets, evaluate models and deploy Python machine learning workflows. Familiarity with NLP, scikit-learn and cloud tooling is valued.',
     'Hyderabad, India', 1100000, 1, CURRENT_TIMESTAMP),
    ('demo-frontend', 'Frontend Engineer', 'Brightworks',
     'Create accessible web experiences with React and TypeScript. Partner with design and API teams to improve performance and quality.',
     'Pune, India', 1000000, 1, CURRENT_TIMESTAMP),
    ('demo-cloud', 'Cloud Platform Developer', 'Aster Digital',
     'Develop cloud-native Java services and deployment pipelines using Spring Boot, AWS, Docker, Kubernetes and observability tools.',
     'Chennai, India', 1800000, 3, CURRENT_TIMESTAMP),
    ('demo-analyst', 'Data Analyst', 'Fieldnote',
     'Translate business questions into reliable metrics. Write SQL, create dashboards and communicate findings with product teams.',
     'Remote, India', 850000, 0, CURRENT_TIMESTAMP),
    ('demo-qa', 'QA Automation Engineer', 'Copperline Tech',
     'Build maintainable test automation for web services using Java, Selenium, REST APIs and CI pipelines.',
     'Bengaluru, India', 950000, 1, CURRENT_TIMESTAMP),
    ('demo-product-engineer', 'Product Software Engineer', 'Common Thread',
     'Work across backend services and web product features with Java, Spring Boot, React, PostgreSQL and cloud infrastructure.',
     'Mumbai, India', 1600000, 2, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO job_required_skills (job_id, skill)
VALUES
    ('demo-java-backend', 'Java'), ('demo-java-backend', 'Spring Boot'), ('demo-java-backend', 'REST APIs'), ('demo-java-backend', 'MySQL'), ('demo-java-backend', 'Docker'),
    ('demo-full-stack', 'Java'), ('demo-full-stack', 'Spring Boot'), ('demo-full-stack', 'React'), ('demo-full-stack', 'PostgreSQL'), ('demo-full-stack', 'Testing'),
    ('demo-ml-associate', 'Python'), ('demo-ml-associate', 'Machine Learning'), ('demo-ml-associate', 'NLP'), ('demo-ml-associate', 'SQL'), ('demo-ml-associate', 'AWS'),
    ('demo-frontend', 'React'), ('demo-frontend', 'JavaScript'), ('demo-frontend', 'TypeScript'), ('demo-frontend', 'CSS'), ('demo-frontend', 'Accessibility'),
    ('demo-cloud', 'Java'), ('demo-cloud', 'Spring Boot'), ('demo-cloud', 'AWS'), ('demo-cloud', 'Docker'), ('demo-cloud', 'Kubernetes'),
    ('demo-analyst', 'SQL'), ('demo-analyst', 'Python'), ('demo-analyst', 'Excel'), ('demo-analyst', 'Data Analysis'), ('demo-analyst', 'Tableau'),
    ('demo-qa', 'Java'), ('demo-qa', 'Selenium'), ('demo-qa', 'Testing'), ('demo-qa', 'REST APIs'), ('demo-qa', 'CI/CD'),
    ('demo-product-engineer', 'Java'), ('demo-product-engineer', 'Spring Boot'), ('demo-product-engineer', 'React'), ('demo-product-engineer', 'PostgreSQL'), ('demo-product-engineer', 'AWS')
ON DUPLICATE KEY UPDATE skill = VALUES(skill);
