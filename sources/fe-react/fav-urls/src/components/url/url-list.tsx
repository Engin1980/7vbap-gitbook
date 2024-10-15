import {useEffect, useState} from "react";
import {UrlView} from "../../model/dtos/url-view";
import axios from "axios";

function UrlList(){
  const [urls, setUrls] = useState<UrlView[]>([]);

  useEffect(() => {
    (async () => {
      try {
        const res = await axios.get("http://localhost:32123/v1/url/1");
        setUrls(res.data);
      } catch (err) {
        console.error(err);
      }
    })();
  }, []);

  return (
    <div>
      <h1>Your Links</h1>
      <div>
        {urls.map(url => (<div className="urlRow" key={url.urlId}>
          <div className="urlTitle">{url.title}</div>
          <div><a href={url.address} rel="noreferrer" target="_blank">{url.address}</a></div>
        </div>))}
      </div>
    </div>
  );
}

export default UrlList;