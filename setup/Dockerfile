FROM docker.elastic.co/elasticsearch/elasticsearch:6.5.4
RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install analysis-icu 
RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install org.wikimedia.search:extra:6.5.4 
RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install org.wikimedia.search:extra-analysis-homoglyph:6.5.4-wmf13
EXPOSE 9200 9300
