import requests,json

# url and token from coagis.colife.org.tw
url_token = {'url' : 'https://coagis.colife.org.tw/arcgis/rest/services/COA_Data/相片位置/MapServer/exts/RestSOE_COA_Data/CalCadAddr', \
             'token' : 'sRwtJZ5mdzMWMzlReMnaRe9GWIYHVKc1xP6OycuBsFN4gO36FdrdyEBokgAuK_kVtHnySbr-cHJOggBGd5Ry_w..'}

# url and token from map.coa.gov.tw
# url_token = {'url' : 'http://map.coa.gov.tw/arcgis/rest/services/COA_Data/相片位置/MapServer/exts/RestSOE_COA_Data/CalCadAddr', \
#              'token' : 'RwtJZ5mdzMWMzlReMnaRYGH08TF4XVeDF5MN1-uhtlwvQx3cboOBjIMXW-0HZd8Mh8OC-0kiT7xlhAjh8UuRg..'}

headers = {'Accept' : 'application/json, text/javascript, */*; q=0.01' , \
           'Origin' : 'null' , \
           'User-Agent' : 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36' , \
           'Content-Type' : 'application/x-www-form-urlencoded; charset=UTF-8' , \
           'Accept-Encoding' : 'gzip, deflate' , \
           'Accept-Language' : 'zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7'}

payload = {'Ver' : '106Q4' , \
           'Lon' : 121.036185 , \
           'Lat' : 24.823243 , \
           'Direction' : 177.118896 , \
           'Distance' : 30 ,\
           'f' : 'json' , \
           'token' : url_token['token']
           }

r = requests.post(url_token['url'], data = payload, headers = headers)
print(r.text)
