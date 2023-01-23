#!/bin/bash
### sbatch config parameters must start with #SBATCH and must precede any other command. to ignore just add another # - like so ##SBATCH
#SBATCH --partition main ### specify partition name where to run a job
#SBATCH --time 7-00:00:00 ### limit the time of job running. Format: D-H:MM:SS
#SBATCH --job-name run_job ### name of the job. replace my_job with your desired job name
#SBATCH --output run_job.out ### output log for running job - %J is the job number variable
#SBATCH --mail-user=tomya@post.bgu.ac.il ### users email for sending job status notifications ñ replace with yours
#SBATCH --mail-type=BEGIN,END,FAIL ### conditions when to send the email. ALL,BEGIN,END,FAIL, REQUEU, NONE
#SBATCH --mem=250G ### total amount of RAM // 500
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=32 ##. // max 128

### Start you code below ####
module load anaconda ### load anaconda module
source activate bpjs_liveness ### activating Conda environment. Environment must be configured before running the job
cd ~/repos/BPjsLiveness/ || exit
export MAVEN_OPTS="-Xms250g -Xmx250g"
mvn compile > /dev/null 2>&1
for i in $(seq 6 7); do
	for b in $(seq 3 3); do
	  echo "map_${i}_${b}"
		mvn exec:java -D"exec.args"="map_${i}_${b}"
	done
done