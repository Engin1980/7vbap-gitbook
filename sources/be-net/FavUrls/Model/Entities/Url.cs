using System.ComponentModel.DataAnnotations.Schema;

namespace FavUrls.Model.Entities
{
  public class Url
  {
    //TODO check if this is needed
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public int UrlId { get; set; }
    public string Title { get; set; } = null!;
    public string Address { get; set; } = null!;
    public AppUser AppUser { get; set; } = null!;
    public List<Tag> Tags { get; set; } = [];
  }
}
