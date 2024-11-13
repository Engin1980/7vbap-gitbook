import {useEffect, useState} from "react";
import {UrlView} from "../../model/url-view";
import "./url-list.css";
import UrlCreatePopup from "./url-create-popup";
import useHttp from "../../hooks/use-http";
import {useLoggedUser} from "../../hooks/use-logged-user";
import {toast} from "react-toastify";

function UrlList() {
  const [urls, setUrls] = useState<UrlView[]>([]);
  const [refresh, setRefresh] = useState<boolean>(true);
  const http = useHttp();
  const doRefresh: () => void = () => setRefresh(true);
  const {loggedUser} = useLoggedUser();

  const appUserId = loggedUser?.appUserId ?? 0;

  useEffect(() => {
    (async () => {
      if (refresh) {
        setRefresh(false);
        try {
          const res = await http.get<UrlView[]>(`/v1/url/${appUserId}`);
          setUrls(res);
        } catch (err) {
          console.error(err);
          toast.error("Failed to get data.");
        }
      }
    })();
  }, [refresh, http, appUserId]);

  return (
    <div>
      <h1>Your Links</h1>
      <div>
        <UrlCreatePopup appUserId={1} refresh={doRefresh}/>
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