# Micro-Cluster Lab Using Docker, To Experiment With Spark & Dask on Yarn
For more details about this project please refer to [my article](https://lemaizi.com/blog/creating-your-own-micro-cluster-lab-using-docker-to-experiment-with-spark-dask-on-yarn/) where I explain the motivations and how to recreate it by yourself.

### Project Folder Tree

```
├── docker-compose.yml
├── Dockerfile
├── confs
│   ├── config
│   ├── core-site.xml
│   ├── hdfs-site.xml
│   ├── mapred-site.xml
│   ├── slaves
│   ├── spark-defaults.conf
│   └── yarn-site.xml
└── script_files
    └── bootstrap.sh
```



### Create the base container image

```bash
docker build . -t cluster-base
```

### Run the cluster or micro-lab

```bash
docker-compose up -d
```

### Yarn resource manager UI

Access the Yarn resource manager UI using the following link : http://localhost:8088/cluster/nodes

![yarn ui](img/yarn_rm_ui.png)

### Stopping the micro-lab

```
docker-compose down
```

