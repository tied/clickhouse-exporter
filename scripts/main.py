import json
from pathlib import Path
from tqdm import tqdm
from requests import Session
from requests.auth import HTTPBasicAuth


def get_config(config_path: Path):
    with open(config_path, 'r') as f:
        return json.load(f)


config = get_config(Path('config.json'))
jira_url = config['jira_url']
rest_path = config['rest_path']


def get_rest_url(rest: str) -> str:
    return f'{jira_url}{rest_path}{rest}'


def get_session() -> Session:
    new_session = Session()
    if 'cert_file' in config:
        new_session.cert = config['cert_file']
    new_session.auth = HTTPBasicAuth(config['login'], config['password'])
    return new_session


session = get_session()

jql = 'created >= "2022-05-16 {:02d}:00" and created < "2022-05-16 {:02d}:00"'

with open('out.csv', 'w') as out:
    for hour in tqdm(range(8, 20), desc='Час'):
        issues = session.post(get_rest_url('/search'),
                              json={'jql': jql.format(hour, hour + 1),
                                    'maxResults': -1}).json()['issues']
        for issue in tqdm(issues, desc='Issue', leave=False):
            key = issue['key']
            issue_created = issue['fields']['created']
            issue_data = session.get(get_rest_url(f'/issue/{key}?expand=changelog')).json()
            changelog = issue_data['changelog']
            out.write(f'{issue_created},{key},{issue_created}\n')
            for history in changelog['histories']:
                history_created = history['created']
                out.write(f'{issue_created},{key},{history_created}\n')
