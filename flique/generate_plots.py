import numpy as np
import csv
import matplotlib.pyplot as plt

import os
import glob

RESULTS_PATH = 'results/'
STRATEGIES = ['OBFS', 'OMBS', 'FLIQUE', 'BFS']
QUERIES = ['S1', 'S2', 'S3', 'S4', 'S5', 'S6', 'S7', 'S8', 'S9', 'S10', 'S11', 'S12', 'S13', 'S14', 'C1', 'C2', 'C3', 'C4', 'C5', 'C6', 'C7', 'C8', 'C9', 'C10', 'L1', 'L2', 'L3', 'L4', 'L5', 'L6', 'L7', 'L8']
RELAXED_QUERIES = ['S8', 'S10', 'C5', 'C8', 'C9', 'C10', 'L5', 'L6', 'L7', 'L8']
ORIGINAL_QUERIES_SUB = ['S1', 'S6', 'S9', 'C3', 'L1', 'L3']

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


def generate_time_for_fist_result_plot_strategies(results, queries=RELAXED_QUERIES, autolabels=False):
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
        autolabel(bfs_rects)
        autolabel(obfs_rects)
        autolabel(ombs_rects)
        autolabel(flique_rects)

    fig.tight_layout()
    plt.show()


def generate_time_for_fist_result_plot_flique(results, queries=QUERIES, autolabels=False):
    labels = queries
    labels = [f"{label}  {label}'" if is_relaxed(label, results) else label for label in labels]
    relax_results = get_list_values(results, 'FLIQUE', 'FirstResultTime', queries, True).astype(int)
    no_relax_results = get_list_values(results, 'FLIQUE', 'FirstResultTime', queries, False).astype(int)

    x = np.arange(len(labels))
    bar_width = 0.35

    fig, ax = plt.subplots(figsize=(16, 6))
    relax_rects = ax.bar(x + (0.5*bar_width), relax_results, bar_width, label='FLiQuE')
    no_relax_rects = ax.bar(x - (0.5*bar_width), no_relax_results, bar_width, label='CostFed')

    ax.set_ylabel('Time for first result (ms)')
    #plt.yscale("log") #logarithmic scale
    ax.set_xlabel('Queries')
    ax.set_xticks(x)
    ax.set_xticklabels(labels)
    ax.legend()

    def autolabel(rects1, rects2):
        for rect1, rect2 in zip(rects1, rects2):
            height1 = int(rect1.get_height())
            height2 = int(rect2.get_height())
            if height1 > 0:
                ax.annotate('{}'.format(height1),
                            xy=(rect1.get_x() + rect1.get_width() / 2, height1),
                            xytext=(0, 3),  # 3 points vertical offset
                            textcoords="offset points",
                            ha='center', va='bottom')
            if height2 > 0:
                offset = 0
                if abs(height1 - height2) < 1000:
                    offset = 13
                ax.annotate('{}'.format(height2),
                            xy=(rect2.get_x() + rect1.get_width() / 2, height2),
                            xytext=(0, 3 + offset),  # 3 points vertical offset
                            textcoords="offset points",
                            ha='center', va='bottom')
    if autolabels:
        autolabel(no_relax_rects, relax_rects)

    fig.tight_layout()
    plt.show()


def generate_nb_gen_exec_result_plot_flique(results, queries=RELAXED_QUERIES, autolabels=False):
    labels = queries
    labels = [f"{label}'" for label in labels]
    nb_gen_results = get_list_values(results, 'FLIQUE', 'nbGeneratedRelaxedQueries', queries, True).astype(int)
    nb_exec_results = get_list_values(results, 'FLIQUE', 'nbEvaluatedRelaxedQueries', queries, True).astype(int)

    x = np.arange(len(labels))
    bar_width = 0.5

    fig, ax = plt.subplots(figsize=(13, 6))
    p1 = plt.bar(x, nb_gen_results, bar_width, label='Generated')
    p2 = plt.bar(x, nb_exec_results, bar_width, bottom=nb_gen_results, label='Executed')

    ax.set_ylabel('Number of failing relaxed queries')
    #plt.yscale("log") #logarithmic scale
    ax.set_xlabel('Candidate queries')
    ax.set_xticks(x)
    ax.set_xticklabels(labels)
    ax.legend()

    fig.tight_layout()
    plt.show()


def get_statistics(results, metric_name, strategy=None, queries=QUERIES, relax=True):
    metric_times = []
    for strat in STRATEGIES:
        if strategy:
            metric_times.extend(get_list_values(results, strategy, metric_name, queries, relax))
            break
        metric_times.extend(get_list_values(results, strat, metric_name, QUERIES, relax))
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
    for value in results[query][True]['FLIQUE'].get('nbGeneratedRelaxedQueries', []):
        if value > 0:
            return True
    return False


results = get_result_dict()
#generate_time_for_fist_result_plot_flique(results, RELAXED_QUERIES, autolabels=True)
#generate_time_for_fist_result_plot_flique(results, [query for query in QUERIES if query not in RELAXED_QUERIES], autolabels=True)
#get_statistics(results, 'LicenseCheckTime')
#get_statistics(results, 'nbGeneratedRelaxedQueries', 'FLIQUE', RELAXED_QUERIES, True)
#get_statistics(results, 'nbEvaluatedRelaxedQueries', 'FLIQUE', RELAXED_QUERIES, True)
#get_statistics(results, 'ResultSimilarity', 'FLIQUE')
#get_statistics(results, 'totalExecTime')
#get_statistics(results, 'FirstResultTime', 'FLIQUE', [query for query in QUERIES if query in ORIGINAL_QUERIES_SUB], True)
#get_statistics(results, 'FirstResultTime', 'FLIQUE', [query for query in QUERIES if query in ORIGINAL_QUERIES_SUB], False)
#get_statistics(results, 'nbFederations', 'FLIQUE', [query for query in QUERIES if query in ORIGINAL_QUERIES_SUB], True)
#generate_nb_gen_exec_result_plot_flique(results, RELAXED_QUERIES)