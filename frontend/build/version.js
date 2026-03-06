/**
 * Generate build version metadata.
 */

const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const outputDir = path.resolve(__dirname, '..', 'dist');
const outputFile = path.join(outputDir, 'version.txt');

const buildDate = new Date().toLocaleString();

const runGit = (command) => {
    try {
        return execSync(command, { encoding: 'utf8' }).trim();
    } catch {
        return 'unknown';
    }
};

const gitBranch = runGit('git symbolic-ref --short -q HEAD');
const lastCommitId = runGit('git rev-parse --short HEAD');

if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
}

const content = `构建时间: ${buildDate}\n构建分支: ${gitBranch}\nCommit ID: ${lastCommitId}\n`;

fs.writeFileSync(outputFile, content, 'utf8');
console.log(content);
