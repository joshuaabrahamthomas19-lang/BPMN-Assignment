"""Structural and restricted DMN rule checks. Run with Python 3 from any folder."""
from pathlib import Path
import json,re,operator,xml.etree.ElementTree as E
R=Path(__file__).resolve().parents[4]
N={'b':'http://www.omg.org/spec/BPMN/20100524/MODEL','z':'http://camunda.org/schema/zeebe/1.0','d':'https://www.omg.org/spec/DMN/20191111/MODEL/','di':'http://www.omg.org/spec/BPMN/20100524/DI'}
def evaluate(file,values):
    table=E.parse(file).find('.//d:decisionTable',N)
    keys=[el.findtext('d:inputExpression/d:text',namespaces=N) for el in table.findall('d:input',N)]
    outputs=[el.get('name') for el in table.findall('d:output',N)]
    def match(test,value):
        if ',' in test:return any(match(part.strip(),value) for part in test.split(','))
        if test=='-':return True
        if test.startswith('"'):return json.loads(test)==value
        if test.startswith('['):
            lo,hi=map(float,test[1:-1].split('..'));return lo<=value<=hi
        m=re.fullmatch(r'(>=|<=|>|<)\s*([0-9.]+)',test);assert m,test
        return {'>=':operator.ge,'<=':operator.le,'>':operator.gt,'<':operator.lt}[m[1]](value,float(m[2]))
    for rule in table.findall('d:rule',N):
        tests=[el.findtext('d:text',namespaces=N) for el in rule.findall('d:inputEntry',N)]
        if all(match(test,values[key]) for key,test in zip(keys,tests)):
            return dict(zip(outputs,[json.loads(el.findtext('d:text',namespaces=N)) for el in rule.findall('d:outputEntry',N)]))
    raise AssertionError('No matching rule')

decision_ids={E.parse(p).find('d:decision',N).get('id') for p in R.rglob('*.dmn')}
forms={json.loads(p.read_text())['id'] for p in R.rglob('*.form')}
for p in R.rglob('*.bpmn'):
    root=E.parse(p).getroot();process=root.find('b:process',N)
    assert process.get('isExecutable')=='true'
    ids=[el.get('id') for el in root.iter() if el.get('id')];assert len(ids)==len(set(ids))
    flows=process.findall('b:sequenceFlow',N)
    nodes={el.get('id'):el for el in process if el.tag.rsplit('}',1)[-1] in ['startEvent','endEvent','serviceTask','userTask','businessRuleTask','exclusiveGateway','boundaryEvent']}
    for f in flows:
        a,b=f.get('sourceRef'),f.get('targetRef');assert a in nodes and b in nodes
        assert f.get('id') in [x.text for x in nodes[a].findall('b:outgoing',N)]
        assert f.get('id') in [x.text for x in nodes[b].findall('b:incoming',N)]
    for node in nodes.values():
        for incoming in node.findall('b:incoming',N):assert any(f.get('id')==incoming.text and f.get('targetRef')==node.get('id') for f in flows)
        for outgoing in node.findall('b:outgoing',N):assert any(f.get('id')==outgoing.text and f.get('sourceRef')==node.get('id') for f in flows)
        for d in node.findall('.//z:calledDecision',N):assert d.get('decisionId') in decision_ids
        for f in node.findall('.//z:formDefinition',N):assert f.get('formId') in forms
    assert {el.get('bpmnElement') for el in root.findall('.//di:BPMNShape',N)}==set(nodes)
    assert {el.get('bpmnElement') for el in root.findall('.//di:BPMNEdge',N)}=={el.get('id') for el in flows}
    print('PASS structure:',p.name)
cases=[]
loan=R/'lab1-loan-approval/loan_classification.dmn'
for score,income,amount,result in [(770,6200,30000,'Approved'),(740,5000,40000,'Approved'),(739,5000,40000,'Manual Review'),(720,7500,60000,'Manual Review'),(690,4200,20000,'Approved'),(620,3500,20000,'Manual Review'),(550,2000,10000,'Rejected'),(599,5000,10000,'Rejected')]:
    cases.append((loan,dict(creditScore=score,monthlyIncome=income,requestedAmount=amount),'classification',result))
order=R/'lab2-ecommerce-fulfillment/order_priority.dmn'
for total,category,pref,priority,review in [(1200,'REGULAR','STANDARD','EXPRESS',True),(1199,'REGULAR','STANDARD','STANDARD',False),(550,'VIP','EXPRESS','CRITICAL',False),(550,'REGULAR','EXPRESS','HIGH',False),(60,'REGULAR','STANDARD','LOW',False)]:
    values=dict(orderTotal=total,customerCategory=category,deliveryPreference=pref)
    cases.extend([(order,values,'deliveryPriority',priority),(order,values,'requiresManualVerification',review)])
care=R/'lab3-hospital-triage/patient_triage.dmn'
for urgency,symptom,age,result in [('CRITICAL','CHEST_PAIN',32,'Urgent Assessment'),('MODERATE','FEVER_HIGH',70,'Priority Review'),('MODERATE','FEVER_HIGH',6,'Priority Review'),('LOW','ROUTINE_CHECK',32,'Routine')]:
    cases.append((care,dict(urgencyIndicator=urgency,symptomsSummary=symptom,patientAge=age),'triageCategory',result))
for file,values,key,result in cases:assert evaluate(file,values)[key]==result,(file,values,key)
print(f'PASS {len(cases)} rule-output checks using a restricted evaluator, not a Camunda engine')
for p in R.rglob('*.json'):json.loads(p.read_text())
print('PASS JSON test inputs and form resources')
