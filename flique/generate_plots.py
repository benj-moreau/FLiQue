import numpy as np
import csv
import matplotlib.pyplot as plt

import os
import glob

RESULTS_PATH = 'results/'
STRATEGIES = ['OBFS', 'OMBS', 'FLIQUE', 'BFS']
QUERIES = ['S1', 'S2', 'S3', 'S4', 'S5', 'S6', 'S7', 'S8', 'S9', 'S10', 'S11', 'S12', 'S13', 'S14', 'C1', 'C2', 'C3', 'C4', 'C5', 'C6', 'C7', 'C8', 'C9', 'C10', 'L1', 'L2', 'L3', 'L4', 'L5', 'L6', 'L7', 'L8', 'CH1', 'CH2', 'CH3', 'CH4', 'CH5', 'CH6', 'CH7', 'CH8']


def result_files():
    for filename in glob.glob(os.path.join(RESULTS_PATH, '*.csv')):
        with open(filename) as csv_file:
            strategy = None
            for strat in STRATEGIES:
                if strat.upper() in filename:
                    strategy = strat
                    break
            line_count = 0
            csv_reader = csv.reader(csv_file, delimiter=';')
            result_idx = {}
            res = {}
            for row in csv_reader:
                if line_count == 0:
                    # header
                    for index, column_name in enumerate(row, start=0):
                        result_idx[column_name] = index
                if line_count == 1:
                    # values
                    for column_name in result_idx:
                        res[column_name] = row[result_idx[column_name]]
                    res['Strategy'] = strategy
                line_count += 1
            if res:
                yield res


def get_result_dict():
    res = {}
    for query in QUERIES:
        res[query] = {}
        for strategy in STRATEGIES:
            res[query][strategy] = {}
    for exp_result in result_files():
        res[exp_result['Query']][exp_result['Strategy']] = exp_result
    return res


def get_list_values(results, strategy, metric_name, queries):
    array = []
    strategy = strategy.upper()
    for query in queries:
        value = results[query][strategy].get(metric_name)
        if value and value != 'null':
            value = int(value)
        else:
            value = 0
        array.append(value)
    return array


def generate_time_for_fist_result_plot(results, queries=QUERIES, autolabels=False):
    labels = queries
    bfs_results = get_list_values(results, 'BFS', 'FirstResultTime', queries)
    obfs_results = get_list_values(results, 'OBFS', 'FirstResultTime', queries)
    ombs_results = get_list_values(results, 'OMBS', 'FirstResultTime', queries)
    flique_results = get_list_values(results, 'FLIQUE', 'FirstResultTime', queries)

    x = np.arange(len(labels))
    bar_width = 0.2

    fig, ax = plt.subplots(figsize=(30, 10))
    bfs_rects = ax.bar(x - (1.5*bar_width), bfs_results, bar_width, label='BFS')
    obfs_rects = ax.bar(x - (0.5*bar_width), obfs_results, bar_width, label='OBFS')
    ombs_rects = ax.bar(x + (0.5*bar_width), ombs_results, bar_width, label='OMBS')
    flique_rects = ax.bar(x + (1.5*bar_width), flique_results, bar_width, label='FLiQuE')

    ax.set_ylabel('Time for first result (ms)')
    ax.set_xlabel('Queries')
    ax.set_xticks(x)
    ax.set_xticklabels(labels)
    ax.legend()

    def autolabel(rects):
        for rect in rects:
            height = rect.get_height()
            if height > 0:
                ax.annotate('{}'.format(height),
                            xy=(rect.get_x() + rect.get_width() / 2, height),
                            xytext=(0, 3),  # 3 points vertical offset
                            textcoords="offset points",
                            ha='center', va='bottom')
    if autolabels:
        autolabel(bfs_rects)
        autolabel(obfs_rects)
        autolabel(ombs_rects)
        autolabel(flique_rects)

    fig.tight_layout()
    plt.show()


def get_statistics(results, metric_name, strategy=None):
    metric_times = []
    for strat in STRATEGIES:
        if strategy:
            metric_times.extend(get_list_values(results, strategy, metric_name, QUERIES))
            break
        metric_times.extend(get_list_values(results, strat, metric_name, QUERIES))
    license_check_times = list(filter((0).__ne__, metric_times))
    license_check_times = np.asarray(license_check_times)
    if not strategy: strategy = 'all'
    print(f'--------------------------------\n {metric_name} (ms):\n --------------------------------')
    if license_check_times.size > 0:
        print(f'strategy {strategy}')
        print(f'generated from {np.alen(license_check_times)} executions')
        print(f'min: {np.amin(license_check_times)}')
        print(f'max: {np.amax(license_check_times)}')
        print(f'average: {np.mean(license_check_times)}')
    else :
        print(f'Zero-size array')


results = get_result_dict()
generate_time_for_fist_result_plot(results, autolabels=True)
get_statistics(results, 'LicenseCheckTime')
get_statistics(results, 'nbGeneratedRelaxedQueries', 'FLIQUE')
get_statistics(results, 'nbEvaluatedRelaxedQueries', 'FLIQUE')
