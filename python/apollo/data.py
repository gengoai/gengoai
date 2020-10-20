from collections import defaultdict
import json
import numpy as np
from typing import Set, Iterable, Union, Dict, Any


class DataSet:
    """
    A DataSet represents a series of observations (input and output). DataSets are generally constructed via Java
    using the Apollo library. Implementations should implement the special __getitem__, __setitem_, and __len__
    methods.
    """

    def limit(self, n: int) -> 'Dataset':
        """
        Limits the items in the dataset to the first N
        :param n: the number of items to limit the dataset to
        :return: this dataset
        """
        raise NotImplementedError

    def observations(self) -> Set[str]:
        """
        :return: The set of observation names in this dataset.
        """
        raise NotImplementedError()

    def dimension(self, name: str) -> int:
        """
        Determines the dimension of the given observation.
        :param name:  The name of observation
        :return:  The dimension of the observation or 0 if not defined
        """
        raise NotImplementedError()

    def select(self, names: Union[Iterable[str], str]) -> Set[Dict[str, Any]]:
        """
        Selects a subset of the observations in this DataSet as a dictionary of name and values.
        :param names: The name(s) of the observations to select
        :return: a dictionary of name and values.
        """
        raise NotImplementedError()

    def __getitem__(self, item):
        raise NotImplementedError()

    def __setitem__(self, key, value):
        raise NotImplementedError()

    def __len__(self):
        raise NotImplementedError()


class ApolloDataSet(DataSet):
    """
    Base class for Apollo DataSets created via Java
    """

    def __init__(self, data_file: str):
        super(ApolloDataSet, self).__init__()
        self.dimensions = None
        self.data = None
        self.size = 0
        self._parse_data(data_file)

    def _parse_data(self, data_file: str):
        raise NotImplementedError()

    def limit(self, n: int) -> DataSet:
        __doc__ = DataSet.__doc__
        for name, nd in self.data.items():
            self.data[name] = nd[:n]
        self.size = min(self.size, n)
        return self

    def __getitem__(self, item):
        return self.data[item]

    def __setitem__(self, key, value):
        self.data[key] = value

    def __len__(self):
        return self.size

    def observations(self) -> Set[str]:
        __doc__ = DataSet.__doc__
        return self.data.keys()

    def dimension(self, name: str) -> int:
        __doc__ = DataSet.__doc__
        return self.dimensions.get(name, 0)

    def select(self, names: Union[Iterable[str], str]) -> Set[Dict[str, Any]]:
        __doc__ = DataSet.__doc__
        r = dict()
        if isinstance(names, str):
            names = [names]
        for n in names:
            r[n] = self.data[n]
        return r

    def _parse_observation(self, obs):
        vtype = obs["@type"].lower()
        if vtype == "vs":
            return [self._parse_observation(so) for so in obs["seq"]]
        if vtype == "dm" or vtype == "sm":
            matrix = obs["matrix"]
            shape = obs["shape"]
            n = np.array(matrix)
            if shape[3] != 1:
                return n.reshape((shape[2], shape[3]))
            return n.reshape(shape[2])
        if vtype == "v":
            p = obs["p"]
            s = obs["s"]
            if p == "":
                return s
            if s == "":
                return p
            return p + "=" + s


class ApolloJsonDataSet(ApolloDataSet):
    """
    Apollo DataSet created in Java and serialized to a Json file
    """

    def __init__(self, data_file: str):
        super().__init__(data_file)

    def _parse_data(self, data_file: str):
        with open(data_file) as fp:
            data_map = json.load(fp)
        self.dimensions = dict()
        for name, m in data_map["metadata"].items():
            self.dimensions[name] = m.get("dimension", 0)

        self.data = defaultdict(lambda: list())
        for datum in data_map["data"]:
            self.size += 1
            for name, obs in datum.items():
                self.data[name].append(self._parse_observation(obs))


class ApolloSQLDataSet(ApolloDataSet):
    """
    Apollo DataSet created in Java and persisted to disk (SQLite)
    """

    def __init__(self, data_file: str):
        super().__init__(data_file)

    def _parse_data(self, data_file: str):
        import sqlite3
        self.connection = sqlite3.connect(data_file)
        c = self.connection.cursor()
        c.execute("SELECT value FROM metadata WHERE name = '__size__'")
        self.size = int(c.fetchone()[0])
        self.dimensions = dict()
        c.execute("SELECT name,value FROM metadata WHERE name != '__size__'")
        for row in c:
            try:
                m = json.loads(row[1])
                self.dimensions[row[0]] = int(m["dimension"])
            except Exception:
                pass
        c.close()
        self.data = defaultdict(lambda: list())
        for row in self.row_iterator("SELECT * FROM DATA"):
            datum = json.loads(row[0])
            for name, obs in datum.items():
                self.data[name].append(self._parse_observation(obs))

    def row_iterator(self, statement):
        c = self.connection.cursor()
        c.execute(statement)
        for row in c:
            yield row
        c.close()
