import SPARQLWrapper
import logging
import json
import os

from rdflib import URIRef, Literal, Graph #NOTE: RDF in rdflib and SPARQLWrapper gets conficts
from panorama.Selector import *
from panorama.Formatter import *
from panorama.Fresnel import *

from django.template import RequestContext
from django.shortcuts import render_to_response
from django.http import HttpResponse

def queryEndpoint( query ):
        sparql = SPARQLWrapper.SPARQLWrapper("http://dbpedia.org/sparql")
        sparql.setQuery( query )
        sparql.setReturnFormat( SPARQLWrapper.RDF )
        result = sparql.query().convert()
        return result


def extractSolutions( graph ):
        """This function returns the subject of sparql-solutions from the graph"""

        solutions = []

	#for result in graph.subjects(predicate=URIRef('http://www.w3.org/1999/02/22-rdf-syntax-ns#type'), object=URIRef('http://www.w3.org/2005/sparql-results#ResultSet')):
	 #       for solution in graph.objects( subject=result, predicate=URIRef('http://www.w3.org/2005/sparql-results#solution')):
	 #               solutions.append( solution )

	for node in graph.subjects( predicate=URIRef(u'http://www.w3.org/2005/sparql-results#variable'), object=Literal(u'uri') ):
		uri = graph.value( subject=node, predicate=URIRef(u'http://www.w3.org/2005/sparql-results#value') )
		solutions.append( uri )

        return solutions

def extractUris( graph, json_file ):
	f = open( json_file )
	

# Views Start Here

def home(request):
	return render_to_response( 'home.html', {}, context_instance=RequestContext(request) )

def askPage(request):
	
	logging.basicConfig( filename="panorama.log", level=logging.DEBUG )
	#TODO: from question get the SPARQL Query, integrate with backend
	question = request.POST['question']
	if question == '':
		return redirect( home )

	#use the jar file

	#read the json file
	module_dir = os.path.dirname(__file__)
	file_path = os.path.join(module_dir, 'output.json')
	json_file = open( file_path )
	data = json.load( json_file )
	json_file.close()

	#check for other result types
	uris = []
	for result in data["result-set"]:
		if result["type"] == "uri":
			uris.append( result["value"] )

	#Using panorama
	solution_graph = Graph()
	print "Downloading resources:"
	#TODO: check efficiency
	for uri in uris:
		print "Downloading " , uri
		solution_graph.parse( uri )
	
	print "RDF Graph:"
	for term in solution_graph:
		logging.debug(term)
	
        print "Creating Fresnel Graph"
	f = open('panorama/data/person_foaf.n3')
        fresnel_data = f.read()
        fresnel = Fresnel( fresnel_data )

	print "Making selection"
        selector = Selector( fresnel , solution_graph)
        selector.select()
	print "Formatting"
        formatter = Formatter( selector )
        formatter.format()
        data = ""
	for resource in formatter.result:
                data += resource.render()

	return render_to_response( 'answer.html', {'result':data,'question':question, }, context_instance=RequestContext(request) )

