def call(Map config = [:]) {

    // -------------
    // 1. Parameters
    // -------------
    String playbook   = config.get('playbook', 'playbook.yml')
    String inventory  = config.get('inventory', '')
    String extraVars  = config.get('extraVars', '')
    String workingDir = config.get('workingDir', '.')

    echo "==========================================="
    echo "       Starting Ansible Playbook CI        "
    echo "==========================================="
    echo "Playbook   : ${playbook}"
    echo "Inventory  : ${inventory ?: 'none'}"
    echo "WorkingDir : ${workingDir}"
    echo "ExtraVars  : ${extraVars ?: 'none'}"

    // Build common options string for ansible-playbook
    def ansibleOptions = ""
    if (inventory?.trim()) {
        ansibleOptions += " -i ${inventory}"
    }
    if (extraVars?.trim()) {
        ansibleOptions += " ${extraVars}"
    }

    // ------------------
    // 2. YAML Validation
    // ------------------
    stage('YAML Validation') {
        dir(workingDir) {
            echo "[YAML] Validating playbook: ${playbook}"
            sh "yamllint ${playbook}"
        }
    }

    // -----------------
    // 3. Ansible Linting
    // -----------------
    stage('Ansible Lint') {
        dir(workingDir) {
            echo "[LINT] Running ansible-lint on playbook: ${playbook}"
            sh "ansible-lint ${playbook}"
        }
    }

    // -----------------------
    // 4. Playbook Syntax Check
    // -----------------------
    stage('Playbook Syntax Check') {
        dir(workingDir) {
            echo "[CHECK] ansible-playbook --syntax-check"
            sh "ansible-playbook ${playbook} ${ansibleOptions} --syntax-check"
        }
    }

    // -----------------------
    // 5. Playbook Dry Run
    // -----------------------
    stage('Playbook Dry Run') {
        dir(workingDir) {
            echo "[DRY-RUN] ansible-playbook --check"
            sh "ansible-playbook ${playbook} ${ansibleOptions} --check"
        }
    }

    echo "==========================================="
    echo "     Ansible Playbook CI Validation Done   "
    echo "==========================================="
}
