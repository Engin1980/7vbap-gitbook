import {useEffect, useState} from "react";
import {UrlView} from "../../model/dtos/url-view";
import axios from "axios";
import "./url-list.css";
import UrlCreatePopup from "./url-create-popup";
import useHttp from "../../hooks/use-http";

function UrlList(){
  const [urls, setUrls] = useState<UrlView[]>([]);
  const [refresh, setRefresh] = useState<boolean>(true);
  const http = useHttp();
  const doRefresh : () => void =  () => setRefresh(true);

  useEffect(() => {
    (async () => {
      if (refresh){
        setRefresh(false);
        try {
          const res = await http.get<UrlView[]>("/v1/url/1");
          setUrls(res);
        } catch (err) {
          console.error(err);
        }
      }
    })();
  }, [refresh]);

  return (
    <div>
      <h1>Your Links</h1>
      <div>
        <UrlCreatePopup appUserId={1} refresh={doRefresh} />
        {!urls && <div>Loading...</div>}
        {urls && urls.map(url => (<div className="urlRow" key={url.urlId}>
          <div className="urlTitle">{url.title}</div>
          <div><a href={url.address} rel="noreferrer" target="_blank">{url.address}</a></div>
        </div>))}
      </div>
    </div>
  );
}

export default UrlList;