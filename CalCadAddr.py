# -*- coding: UTF-8 -*-
import requests,json,sys,getopt

def main(argv):
   longitude = '' # GPS longitude
   latitude = ''  # GPS latitude
   direction = '' # azimuth
   distance = ''  #focal length
   
   try:
      opts, args = getopt.getopt(argv,"ha:b:c:d:",["longitude=","latitude=","direction=","distance="])
   except getopt.GetoptError:
      print('CalCadAddr.py -a <longitude> -b <latitude> -c <direction> -d <distance>')
      sys.exit(2)
   for opt, arg in opts:
      if opt == '-h':
         print('CalCadAddr.py -i <inputfile> -o <outputfile>')
         sys.exit()
      elif opt in ("-a", "--longitude"):
         longitude = arg
      elif opt in ("-b", "--latitude"):
         latitude = arg
      elif opt in ("-c", "--direction"):
         direction = arg
      elif opt in ("-d", "--distance"):
         distance = arg
         
   # url and token from coagis.colife.org.tw
   url_token = {'url' : 'https://coagis.colife.org.tw/arcgis/rest/services/COA_Data/相片位置/MapServer/exts/RestSOE_COA_Data/CalCadAddr', \
                'token' : 'sRwtJZ5mdzMWMzlReMnaRZQtzou2mkCFx2hvaNQiDl9_DP384A-pHhivEgHFoimUGz3olkqaSWmsDjOODjNmsA..'}

   # url and token from map.coa.gov.tw
   # url_token = {'url' : 'http://map.coa.gov.tw/arcgis/rest/services/COA_Data/相片位置/MapServer/exts/RestSOE_COA_Data/CalCadAddr', \
   #              'token' : 'sRwtJZ5mdzMWMzlReMnaRY-yQNL92OgIwFvDkMUqw_ycJqFTm7t1jjDBb-abCp4W8wlvjfLkCkGMzgVhbhbogw..'}

   headers = {'Accept' : 'application/json, text/javascript, */*; q=0.01' , \
              'Origin' : 'null' , \
              'User-Agent' : 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36' , \
              'Content-Type' : 'application/x-www-form-urlencoded; charset=UTF-8' , \
              'Accept-Encoding' : 'gzip, deflate' , \
              'Accept-Language' : 'zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7'}

   # sample : longitude=121.036185 latitude=24.823243 latitude=177.118896 distance=30
   payload = {'Ver' : '106Q4' , \
              'f' : 'json' , \
              'token' : url_token['token'] , \
              'Lon' : longitude , \
              'Lat' : latitude , \
              'Direction' : direction , \
              'Distance' :  distance
              }

   r = requests.post(url_token['url'], data = payload, headers = headers)
   print(r.text)
   
if __name__ == "__main__":
   main(sys.argv[1:])
