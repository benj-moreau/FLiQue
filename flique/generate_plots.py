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
            relax = True
            for strat in STRATEGIES:
                if strat.upper() in filename:
                    strategy = strat
                    break
            if "relax_false" in filename:
                relax = False
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
                    res['Relax'] = relax
                line_count += 1
            if res and res['validResult'] == 'true':
                yield res


def add_result(res, exp_result):
    if res[exp_result['Query']][exp_result['Relax']][exp_result['Strategy']]:
        res[exp_result['Query']][exp_result['Relax']][exp_result['Strategy']]['FirstResultTime'].append(int(exp_result['FirstResultTime'] if exp_result['FirstResultTime'] != 'null' else 0))
        res[exp_result['Query']][exp_result['Relax']][exp_result['Strategy']]['LicenseCheckTime'].append(int(exp_result['LicenseCheckTime'] if exp_result['LicenseCheckTime'] != 'null' else 0))
        res[exp_result['Query']][exp_result['Relax']][exp_result['Strategy']]['ResultSimilarity'].append(float(exp_result['ResultSimilarity'] if exp_result['ResultSimilarity'] != 'null' else 0.0))
        res[exp_result['Query']][exp_result['Relax']][exp_result['Strategy']]['totalExecTime'].append(int(exp_result['totalExecTime'] if exp_result['totalExecTime'] != 'null' else 0))
        res[exp_result['Query']][exp_result['Relax']][exp_result['Strategy']]['nbGeneratedRelaxedQueries'].append(int(exp_result['nbGeneratedRelaxedQueries'] if exp_result['nbGeneratedRelaxedQueries'] != 'null' else 0))
        res[exp_result['Query']][exp_result['Relax']][exp_result['Strategy']]['nbEvaluatedRelaxedQueries'].append(int(exp_result['nbEvaluatedRelaxedQueries'] if exp_result['nbEvaluatedRelaxedQueries'] != 'null' else 0))
        res[exp_result['Query']][exp_result['Relax']][exp_result['Strategy']]['nbFederations'].append(int(exp_result['nbFederations'] if exp_result['nbFederations'] != 'null' else 0))
    else:
        entry = {
            'Query': exp_result['Query'],
            'Relax': exp_result['Relax'],
            'Strategy': exp_result['Strategy'],
            'FirstResultTime': [int(exp_result['FirstResultTime']) if exp_result['FirstResultTime'] != 'null' else 0],
            'LicenseCheckTime': [int(exp_result['LicenseCheckTime']) if exp_result['LicenseCheckTime'] != 'null' else 0],
            'ResultSimilarity': [float(exp_result['ResultSimilarity']) if exp_result['ResultSimilarity'] != 'null' else 0.0],
            'totalExecTime': [int(exp_result['totalExecTime']) if exp_result['totalExecTime'] != 'null' else 0],
            'nbGeneratedRelaxedQueries': [int(exp_result['nbGeneratedRelaxedQueries']) if exp_result['nbGeneratedRelaxedQueries'] != 'null' else 0],
            'nbEvaluatedRelaxedQueries': [int(exp_result['nbEvaluatedRelaxedQueries']) if exp_result['nbEvaluatedRelaxedQueries'] != 'null' else 0],
            'nbFederations': [int(exp_result['nbFederations'])]
        }
        res[exp_result['Query']][exp_result['Relax']][exp_result['Strategy']] = entry


def get_result_dict():
    res = {}
    for query in QUERIES:
        res[query] = {True: {}, False: {}}
        for strategy in STRATEGIES:
            res[query][True][strategy] = {}
            res[query][False][strategy] = {}
    for exp_result in result_files():
        add_result(res, exp_result)
    return res


def get_list_values(results, strategy, metric_name, queries, relax=True):
    array = []
    strategy = strategy.upper()
    for query in queries:
        query_values = results[query][relax][strategy].get(metric_name)
        if query_values:
            if metric_name == "ResultSimilarity":
                query_values = list(filter((0.0).__ne__, query_values))
            else:
                query_values = list(filter((0).__ne__, query_values))
            if query_values:
                query_values = np.asarray(query_values)
                mean_value = np.mean(query_values)
                array.append(mean_value)
            else:
                array.append(0.0)
        else:
            array.append(0.0)
    return np.asarray(array)


def generate_time_for_fist_result_plot_strategies(results, queries=QUERIES, autolabels=False):
    labels = queries
    labels = [f"{label}'" for label in labels]
    bfs_results = get_list_values(results, 'BFS', 'FirstResultTime', queries).astype(int)
    obfs_results = get_list_values(results, 'OBFS', 'FirstResultTime', queries).astype(int)
    ombs_results = get_list_values(results, 'OMBS', 'FirstResultTime', queries).astype(int)
    flique_results = get_list_values(results, 'FLIQUE', 'FirstResultTime', queries).astype(int)

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


def generate_time_for_fist_result_plot_flique(results, queries=QUERIES, autolabels=False):
    labels = queries
    print(results)
    labels = [f"{label}  {label}'" if is_relaxed(label, results) else label for label in labels]
    relax_results = get_list_values(results, 'FLIQUE', 'FirstResultTime', queries, True).astype(int)
    no_relax_results = get_list_values(results, 'FLIQUE', 'FirstResultTime', queries, False).astype(int)

    x = np.arange(len(labels))
    bar_width = 0.40

    fig, ax = plt.subplots(figsize=(30, 10))
    relax_rects = ax.bar(x + (0.5*bar_width), relax_results, bar_width, label='FLiQuE')
    no_relax_rects = ax.bar(x - (0.5*bar_width), no_relax_results, bar_width, label='CostFed')

    ax.set_ylabel('Time for first result (ms)')
    plt.yscale("log") #logarithmic scale
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
        autolabel(relax_rects)
        autolabel(no_relax_rects)

    fig.tight_layout()
    plt.show()


def get_statistics(results, metric_name, strategy=None):
    metric_times = []
    for strat in STRATEGIES:
        if strategy:
            metric_times.extend(get_list_values(results, strategy, metric_name, QUERIES))
            break
        metric_times.extend(get_list_values(results, strat, metric_name, QUERIES))
    license_check_times = list(filter((0.0).__ne__, metric_times))
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


def is_relaxed(query, results):
    for value in results[query][True]['FLIQUE'].get('nbEvaluatedRelaxedQueries', []):
        if value > 0:
            return True
    return False


results = get_result_dict()
generate_time_for_fist_result_plot_flique(results, autolabels=True)
# generate_time_for_fist_result_plot_strategies(results, autolabels=True)
get_statistics(results, 'LicenseCheckTime')
get_statistics(results, 'nbGeneratedRelaxedQueries', 'FLIQUE')
get_statistics(results, 'nbEvaluatedRelaxedQueries', 'FLIQUE')
