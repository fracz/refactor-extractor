srun -p plgrid-gpu -N 1 --ntasks-per-node=1 -n 1 --gres=gpu:1 -A scqfracz --time=0:30:00 --mem-per-cpu=2GB --pty /bin/bash -l

unset PYTHONPATH
module load plgrid/apps/cuda/8.0
module load plgrid/tools/python/3.6.0
source ~/quality/tensorflow/venv/bin/activate
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/net/people/plgfracz/cudnn/cuda/lib64
python quality.py device:GPU:0
