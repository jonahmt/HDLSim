import subprocess
import os

SRC_PATH = "/Users/jonahtharakan/Desktop/personal_projects/HDLSim/src"

class bcolors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKCYAN = '\033[96m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'

def main():
    build()
    run_all_tests()


def build():

    cmd = ['javac']
    for dirpath, dirnames, filenames in os.walk(SRC_PATH):
        for filename in filenames:
            if filename.endswith(".java"):
                cmd.append(dirpath + '/' + filename)
    cmd.append('-d')
    cmd.append(SRC_PATH + '/../bin')
    subprocess.run(cmd)


def run_all_tests():
    test_dir = SRC_PATH + "/../test/IntegrationTests"
    for dir in os.listdir(test_dir):
        exec_test(test_dir + "/" + dir)
        check_test(test_dir + "/" + dir)


def exec_test(dir):
    src_dir = dir + "/src/"
    cmd = ["java", "-cp", SRC_PATH + "/../bin/", "Source.HDLSim", src_dir]
    subprocess.run(cmd, stdout=subprocess.DEVNULL)


def check_test(dir):
    real_out = dir + "/src/out"
    expected_out = dir + "/expected_out"

    try:
        with open(real_out + "/log.txt") as real_log_file:
            with open(expected_out + "/log.txt") as expected_log_file:
                real_log = real_log_file.read()
                expected_log = expected_log_file.read()
                if real_log != expected_log:
                    print(bcolors.FAIL + "Log files differ!" + bcolors.ENDC)
                    return
    except FileNotFoundError:
        pass


    with open(real_out + "/result.txt") as real_result_file:
        with open(expected_out + "/result.txt") as expected_result_file:
            real_result = real_result_file.read()
            expected_result = expected_result_file.read()
            if real_result != expected_result:
                print(bcolors.FAIL + "Result files differ!" + bcolors.ENDC)
                return

    print(bcolors.OKGREEN + "Test Passed!" + bcolors.ENDC)


if __name__ == '__main__':
    main()