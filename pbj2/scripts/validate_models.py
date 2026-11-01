"""Offline structural checks and rule boundary checks; not an engine execution test."""
from pathlib import Path
import json, operator, re, xml.etree.ElementTree as E

ROOT=Path(__file__).resolve().parents[1]
B='http://www.omg.org/spec/BPMN/20100524/MODEL'
Z='http://camunda.org/schema/zeebe/1.0'
D='https://www.omg.org/spec/DMN/20191111/MODEL/'
DI='http://www.omg.org/spec/BPMN/20100524/DI'
def tag(ns,n): return '{'+ns+'}'+n
def decision(name,values):
    """Evaluate ONLY the numeric comparisons, strings and wildcards used by these tables."""
    table=E.parse(ROOT/'models'/(name+'.dmn')).find('.//'+tag(D,'decisionTable'))
    keys=[n.find(tag(D,'inputExpression')).findtext(tag(D,'text')) for n in table.findall(tag(D,'input'))]
    def match(test,value):
        if test=='-': return True
        if test.startswith('"'): return value==json.loads(test)
        m=re.fullmatch(r'(>=|<=|>|<)\s*([0-9.]+)',test)
        assert m, 'Unsupported unary test: '+test
        return {'>=':operator.ge,'<=':operator.le,'>':operator.gt,'<':operator.lt}[m[1]](value,float(m[2]))
    for rule in table.findall(tag(D,'rule')):
        tests=[n.findtext(tag(D,'text')) for n in rule.findall(tag(D,'inputEntry'))]
        if all(match(t,values[k]) for k,t in zip(keys,tests)):
            return json.loads(rule.find(tag(D,'outputEntry')).findtext(tag(D,'text')))
    raise AssertionError('No matching rule')

def validate():
    decisions={E.parse(p).find(tag(D,'decision')).get('id') for p in (ROOT/'models').glob('*.dmn')}
    forms={json.loads(p.read_text())['id'] for p in (ROOT/'forms').glob('*.form')}
    node_count=0
    for path in (ROOT/'models').glob('*.bpmn'):
        root=E.parse(path).getroot(); ids=[n.get('id') for n in root.iter() if n.get('id')]
        assert len(ids)==len(set(ids)), 'Duplicate ID'
        p=root.find(tag(B,'process')); assert p.get('isExecutable')=='true'
        flows=p.findall(tag(B,'sequenceFlow')); nodes={n.get('id'):n for n in p if n.tag!=tag(B,'sequenceFlow')}
        incoming={n:[] for n in nodes};outgoing={n:[] for n in nodes}
        for f in flows:
            a,b=f.get('sourceRef'),f.get('targetRef');assert a in nodes and b in nodes
            incoming[b].append(f.get('id'));outgoing[a].append(f.get('id'))
        for id,n in nodes.items():
            assert sorted(incoming[id])==sorted(x.text for x in n.findall(tag(B,'incoming')))
            assert sorted(outgoing[id])==sorted(x.text for x in n.findall(tag(B,'outgoing')))
            if n.tag==tag(B,'exclusiveGateway') and len(outgoing[id])>1:
                assert n.get('default') in outgoing[id]
                for f in flows:
                    if f.get('sourceRef')==id and f.get('id')!=n.get('default'): assert f.find(tag(B,'conditionExpression')) is not None
            if n.tag==tag(B,'serviceTask'): assert n.find('.//'+tag(Z,'taskDefinition')) is not None
            if n.tag==tag(B,'userTask'):
                assert n.find('.//'+tag(Z,'userTask')) is not None
                assert n.find('.//'+tag(Z,'formDefinition')).get('formId') in forms
            if n.tag==tag(B,'businessRuleTask'): assert n.find('.//'+tag(Z,'calledDecision')).get('decisionId') in decisions
        starts=[k for k,v in nodes.items() if v.tag==tag(B,'startEvent')]
        visited=set();pending=starts[:]
        while pending:
            n=pending.pop()
            if n in visited: continue
            visited.add(n);pending.extend(f.get('targetRef') for f in flows if f.get('sourceRef')==n)
        assert visited==set(nodes), 'Unreachable nodes'
        reachable_ends={k for k,v in nodes.items() if v.tag==tag(B,'endEvent')}
        while True:
            more={f.get('sourceRef') for f in flows if f.get('targetRef') in reachable_ends}
            if more<=reachable_ends: break
            reachable_ends |= more
        assert reachable_ends==set(nodes),'Path with no end'
        assert {x.get('bpmnElement') for x in root.iter(tag(DI,'BPMNShape'))}==set(nodes)
        assert {x.get('bpmnElement') for x in root.iter(tag(DI,'BPMNEdge'))}=={f.get('id') for f in flows}
        node_count+=len(nodes)
        print('PASS structure:',path.name)
    cases=[('studio_lending_policy',dict(creditScore=s,paymentRatio=r,requestedAmount=a),expected) for s,r,a,expected in [(760,.2,20000,'Approved'),(579,.2,20000,'Rejected'),(720,.30,40000,'Approved'),(720,.3001,40000,'Manual Review'),(720,.45,40000,'Manual Review'),(720,.4501,40000,'Rejected'),(719,.2,20000,'Manual Review'),(760,.2,40001,'Manual Review')]]
    cases += [('studio_dispatch_policy',dict(orderTotal=t,customerCategory=c,deliveryMode=d),expected) for t,c,d,expected in [(1500,'MEMBER','EXPRESS','AUDIT'),(1499,'MEMBER','EXPRESS','PRIORITY'),(100,'REGULAR','EXPRESS','EXPRESS'),(100,'REGULAR','STANDARD','ECONOMY')]]
    cases += [('studio_access_policy',dict(urgency=u,patientAge=a),expected) for u,a,expected in [('CRITICAL',30,'Urgent Assessment'),('MODERATE',30,'Priority Review'),('LOW',5,'Priority Review'),('LOW',6,'Routine'),('LOW',69,'Routine'),('LOW',70,'Priority Review')]]
    for name,values,expected in cases: assert decision(name,values)==expected,(name,values)
    print(f'PASS {len(cases)} DMN rule boundary cases (restricted offline evaluator); {node_count} BPMN nodes')

if __name__=='__main__': validate()
